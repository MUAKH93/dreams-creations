package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.ProductionStartRequest;
import com.dreams.dreamscreations.dto.ProductionStartResponse;
import com.dreams.dreamscreations.dto.ProductionStartResponse.StartedBatchItem;
import com.dreams.dreamscreations.entity.*;
import com.dreams.dreamscreations.repository.*;
import com.dreams.dreamscreations.service.DesignRequiredStageService;
import com.dreams.dreamscreations.service.ModuleAssignmentService;
import com.dreams.dreamscreations.service.ProductionBatchService;
import com.dreams.dreamscreations.service.ProductionWorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductionWorkflowServiceImpl implements ProductionWorkflowService {

    private final DesignRepository designRepo;
    private final SizeRepository sizeRepo;
    private final SuitRepository suitRepo;
    private final SupervisorRepository supervisorRepo;
    private final ProductionModuleRepository moduleRepo;
    private final DesigningWorkTypeRepository designingWorkTypeRepo;
    private final ProductionBatchService batchService;
    private final ModuleAssignmentService assignmentService;
    private final DesignRequiredStageService stagePathService;

    public ProductionWorkflowServiceImpl(DesignRepository designRepo,
                                         SizeRepository sizeRepo,
                                         SuitRepository suitRepo,
                                         SupervisorRepository supervisorRepo,
                                         ProductionModuleRepository moduleRepo,
                                         DesigningWorkTypeRepository designingWorkTypeRepo,
                                         ProductionBatchService batchService,
                                         ModuleAssignmentService assignmentService,
                                         DesignRequiredStageService stagePathService) {
        this.designRepo = designRepo;
        this.sizeRepo = sizeRepo;
        this.suitRepo = suitRepo;
        this.supervisorRepo = supervisorRepo;
        this.moduleRepo = moduleRepo;
        this.designingWorkTypeRepo = designingWorkTypeRepo;
        this.batchService = batchService;
        this.assignmentService = assignmentService;
        this.stagePathService = stagePathService;
    }

    @Override
    @Transactional
    public ProductionStartResponse startProductionOrder(ProductionStartRequest request) {
        validateCommonRequest(request);

        List<ProductionStartRequest.DesignBatchLine> lines = resolveDesignLines(request);
        List<StartedBatchItem> started = new ArrayList<>();

        for (ProductionStartRequest.DesignBatchLine line : lines) {
            started.add(startSingleDesignLine(request, line));
        }

        StartedBatchItem first = started.get(0);
        String batchNumbers = started.stream()
                .map(i -> i.getBatch().getBatchNumber())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String message = started.size() == 1
                ? first.getBatch().getBatchNumber() + " started — "
                + lines.get(0).getQuantity() + " suits sent to Designing"
                : started.size() + " batches started: " + batchNumbers;

        return new ProductionStartResponse(
                first.getBatch(),
                first.getSuit(),
                first.getAssignment(),
                started,
                message
        );
    }

    private StartedBatchItem startSingleDesignLine(ProductionStartRequest request,
                                                   ProductionStartRequest.DesignBatchLine line) {
        if (line.getDesignId() == null) {
            throw new RuntimeException("Design is required for each line");
        }
        if (line.getQuantity() == null || line.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than zero for each design line");
        }
        if (line.getColor() == null || line.getColor().isBlank()) {
            throw new RuntimeException("Color is required for each design line");
        }

        Design design = designRepo.findById(line.getDesignId())
                .orElseThrow(() -> new RuntimeException("Design not found: " + line.getDesignId()));

        if (request.getCategoryId() != null
                && !design.getCategory().getCategoryId().equals(request.getCategoryId())) {
            throw new RuntimeException("Design " + design.getDesignCode()
                    + " does not belong to the chosen category");
        }

        final Size resolvedSize = request.getSizeId() != null
                ? sizeRepo.findById(request.getSizeId())
                    .orElseThrow(() -> new RuntimeException("Size not found: " + request.getSizeId()))
                : null;

        Supervisor supervisor = supervisorRepo.findById(request.getSupervisorId())
                .orElseThrow(() -> new RuntimeException("Supervisor not found: " + request.getSupervisorId()));

        String color = line.getColor().trim();

        Suit suit;
        if (resolvedSize != null) {
            suit = suitRepo.findByDesignAndSizeAndColor(design, resolvedSize, color)
                    .orElseGet(() -> suitRepo.save(Suit.builder()
                            .design(design)
                            .size(resolvedSize)
                            .color(color)
                            .status("active")
                            .build()));
        } else {
            suit = suitRepo.findByDesignAndSizeIsNullAndColor(design, color)
                    .orElseGet(() -> suitRepo.save(Suit.builder()
                            .design(design)
                            .size(null)
                            .color(color)
                            .status("active")
                            .build()));
        }

        LocalDate endDate = request.getExpectedCompletionDate() != null
                ? request.getExpectedCompletionDate()
                : request.getDueDate() != null ? request.getDueDate().toLocalDate() : null;

        String designLabel = line.getDesignLabel();
        if (designLabel == null || designLabel.isBlank()) {
            designLabel = design.getName();
        }

        ProductionBatch batch = ProductionBatch.builder()
                .suit(suit)
                .designLabel(designLabel.trim())
                .articleName(line.getArticleName() != null
                        ? line.getArticleName().trim() : null)
                .totalSuitPlanned(line.getQuantity())
                .expectedCompletionDate(endDate)
                .status("in_progress")
                .startDate(LocalDate.now())
                .build();

        batch = batchService.save(batch);

        ProductionStage firstStage = stagePathService.getFirstStage(design.getDesignId());
        List<ProductionModule> modules = moduleRepo
                .findByStageAndStatusOrderByModuleIdAsc(firstStage, "active");
        if (modules.isEmpty()) {
            throw new RuntimeException("No active module found for stage: " + firstStage.getStageName());
        }

        LocalDateTime dueDateTime = request.getDueDate() != null
                ? request.getDueDate()
                : endDate != null
                    ? endDate.atTime(18, 0)
                    : LocalDateTime.now().plusDays(14);

        ModuleAssignment assignment = ModuleAssignment.builder()
                .batch(batch)
                .module(modules.get(0))
                .supervisor(supervisor)
                .quantitySent(line.getQuantity())
                .dueDate(dueDateTime)
                .designingWorkType(designingWorkTypeRepo.findById(request.getDesigningWorkTypeId())
                        .orElseThrow(() -> new RuntimeException("Designing work type not found")))
                .build();

        assignment = assignmentService.dispatch(assignment);

        return new StartedBatchItem(batch, suit, assignment);
    }

    private List<ProductionStartRequest.DesignBatchLine> resolveDesignLines(ProductionStartRequest request) {
        if (request.getDesignLines() != null && !request.getDesignLines().isEmpty()) {
            return request.getDesignLines();
        }
        if (request.getDesignId() == null) {
            throw new RuntimeException("At least one design is required");
        }
        ProductionStartRequest.DesignBatchLine single = new ProductionStartRequest.DesignBatchLine();
        single.setDesignId(request.getDesignId());
        single.setQuantity(request.getQuantity());
        return List.of(single);
    }

    private void validateCommonRequest(ProductionStartRequest request) {
        boolean hasLines = request.getDesignLines() != null && !request.getDesignLines().isEmpty();
        if (!hasLines) {
            if (request.getDesignId() == null) throw new RuntimeException("Design is required");
            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                throw new RuntimeException("Quantity must be greater than zero");
            }
        }
        if (request.getSupervisorId() == null) {
            throw new RuntimeException("Supervisor is required");
        }
        if (request.getDesigningWorkTypeId() == null) {
            throw new RuntimeException("Designing work type is required");
        }
        if (request.getDueDate() == null && request.getExpectedCompletionDate() == null) {
            throw new RuntimeException("End date (due date) is required");
        }
    }
}
