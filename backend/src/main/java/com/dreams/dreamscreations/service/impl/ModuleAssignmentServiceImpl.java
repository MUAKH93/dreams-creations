package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.DispatchRequest;
import com.dreams.dreamscreations.dto.ReturnRequest;
import com.dreams.dreamscreations.entity.*;
import com.dreams.dreamscreations.repository.*;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.DesignRequiredStageService;
import com.dreams.dreamscreations.service.InventoryService;
import com.dreams.dreamscreations.service.ModuleAssignmentService;
import com.dreams.dreamscreations.service.ProductionSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ModuleAssignmentServiceImpl implements ModuleAssignmentService {

    private final ModuleAssignmentRepository assignmentRepo;
    private final ModuleAssignmentDependencyRepository dependencyRepo;
    private final ProductionBatchRepository batchRepo;
    private final ProductionModuleRepository moduleRepo;
    private final SupervisorRepository supervisorRepo;
    private final DesigningWorkTypeRepository designingWorkTypeRepo;
    private final FillingWorkTypeRepository fillingWorkTypeRepo;
    private final SizeRepository sizeRepo;
    private final SuitRepository suitRepo;
    private final DesignRequiredStageService stagePathService;
    private final InventoryService inventoryService;
    private final CurrentUserService currentUserService;
    private final ProductionSettingsService productionSettingsService;

    public ModuleAssignmentServiceImpl(ModuleAssignmentRepository assignmentRepo,
                                       ModuleAssignmentDependencyRepository dependencyRepo,
                                       ProductionBatchRepository batchRepo,
                                       ProductionModuleRepository moduleRepo,
                                       SupervisorRepository supervisorRepo,
                                       DesigningWorkTypeRepository designingWorkTypeRepo,
                                       FillingWorkTypeRepository fillingWorkTypeRepo,
                                       SizeRepository sizeRepo,
                                       SuitRepository suitRepo,
                                       DesignRequiredStageService stagePathService,
                                       InventoryService inventoryService,
                                       CurrentUserService currentUserService,
                                       ProductionSettingsService productionSettingsService) {
        this.assignmentRepo = assignmentRepo;
        this.dependencyRepo = dependencyRepo;
        this.batchRepo = batchRepo;
        this.moduleRepo = moduleRepo;
        this.supervisorRepo = supervisorRepo;
        this.designingWorkTypeRepo = designingWorkTypeRepo;
        this.fillingWorkTypeRepo = fillingWorkTypeRepo;
        this.sizeRepo = sizeRepo;
        this.suitRepo = suitRepo;
        this.stagePathService = stagePathService;
        this.inventoryService = inventoryService;
        this.currentUserService = currentUserService;
        this.productionSettingsService = productionSettingsService;
    }

    @Override
    @Transactional
    public ModuleAssignment dispatch(DispatchRequest request) {
        if (request.getBatchId() == null || request.getModuleId() == null
                || request.getSupervisorId() == null || request.getDueDate() == null) {
            throw new RuntimeException("Batch, module, supervisor, and due date are required");
        }

        ProductionBatch batch = batchRepo.findById(request.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));
        ProductionModule module = moduleRepo.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found"));
        Supervisor supervisor = supervisorRepo.findById(request.getSupervisorId())
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        ProductionStage stage = module.getStage();
        boolean requiresSkuBreakdown = requiresSkuBreakdown(stage);

        ModuleAssignment assignment = ModuleAssignment.builder()
                .batch(batch)
                .module(module)
                .supervisor(supervisor)
                .dueDate(request.getDueDate())
                .build();

        if (isDesigningStage(stage)) {
            if (request.getDesigningWorkTypeId() == null) {
                throw new RuntimeException("Designing work type is required when dispatching to Designing");
            }
            assignment.setDesigningWorkType(designingWorkTypeRepo.findById(request.getDesigningWorkTypeId())
                    .orElseThrow(() -> new RuntimeException("Designing work type not found")));
        }

        if (isFillingStage(stage)) {
            if (request.getFillingWorkTypeId() == null) {
                throw new RuntimeException("Filling work type is required when dispatching to Filling");
            }
            assignment.setFillingWorkType(fillingWorkTypeRepo.findById(request.getFillingWorkTypeId())
                    .orElseThrow(() -> new RuntimeException("Filling work type not found")));
        }

        if (requiresSkuBreakdown) {
            List<DispatchRequest.SkuLineRequest> lines = request.getSkuLines();
            if (lines == null || lines.isEmpty()) {
                throw new RuntimeException(
                        "Size and color breakdown is required when dispatching to Cutting & Stitching");
            }
            int lineTotal = 0;
            List<ModuleAssignmentSkuLine> skuLines = new ArrayList<>();
            for (DispatchRequest.SkuLineRequest line : lines) {
                if (line.getSizeId() == null || line.getColor() == null || line.getColor().isBlank()) {
                    throw new RuntimeException("Each line needs a size and color");
                }
                if (line.getQuantity() == null || line.getQuantity() <= 0) {
                    throw new RuntimeException("Each line needs quantity greater than zero");
                }
                Size size = sizeRepo.findById(line.getSizeId())
                        .orElseThrow(() -> new RuntimeException("Size not found: " + line.getSizeId()));
                String color = line.getColor().trim();
                skuLines.add(ModuleAssignmentSkuLine.builder()
                        .assignment(assignment)
                        .size(size)
                        .color(color)
                        .quantitySent(line.getQuantity())
                        .build());
                lineTotal += line.getQuantity();
            }
            assignment.setSkuLines(skuLines);
            assignment.setQuantitySent(lineTotal);
        } else {
            if (request.getQuantitySent() == null || request.getQuantitySent() <= 0) {
                throw new RuntimeException("Quantity sent must be greater than zero");
            }
            assignment.setQuantitySent(request.getQuantitySent());
        }

        return dispatchInternal(assignment);
    }

    @Override
    @Transactional
    public ModuleAssignment dispatch(ModuleAssignment assignment) {
        if (assignment.getBatch() != null && assignment.getBatch().getBatchId() != null) {
            ProductionBatch batch = batchRepo.findById(assignment.getBatch().getBatchId())
                    .orElseThrow(() -> new RuntimeException("Batch not found"));
            assignment.setBatch(batch);
        }
        if (assignment.getModule() != null && assignment.getModule().getModuleId() != null) {
            ProductionModule module = moduleRepo.findById(assignment.getModule().getModuleId())
                    .orElseThrow(() -> new RuntimeException("Module not found"));
            assignment.setModule(module);
        }
        if (assignment.getSupervisor() != null && assignment.getSupervisor().getSupervisorId() != null) {
            Supervisor supervisor = supervisorRepo.findById(assignment.getSupervisor().getSupervisorId())
                    .orElseThrow(() -> new RuntimeException("Supervisor not found"));
            assignment.setSupervisor(supervisor);
        }

        ProductionStage stage = assignment.getModule().getStage();
        if (isDesigningStage(stage) && assignment.getDesigningWorkType() == null) {
            throw new RuntimeException("Designing work type is required when dispatching to Designing");
        }

        return dispatchInternal(assignment);
    }

    private ModuleAssignment dispatchInternal(ModuleAssignment assignment) {
        if (assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Due date must be in the future");
        }
        if (assignment.getQuantitySent() <= 0) {
            throw new RuntimeException("Quantity sent must be greater than zero");
        }

        validateStageDispatch(assignment.getBatch(), assignment.getModule(), assignment.getQuantitySent());

        assignment.setStatus("sent");
        assignment.setStartDate(LocalDateTime.now());

        ModuleAssignment saved = assignmentRepo.save(assignment);

        ProductionBatch batch = saved.getBatch();
        if ("planned".equals(batch.getStatus())) {
            batch.setStatus("in_progress");
            batchRepo.save(batch);
        }

        return assignmentRepo.findByIdWithDetails(saved.getAssignmentId()).orElse(saved);
    }

    @Override
    @Transactional
    public ModuleAssignment returnAssignment(Long assignmentId, ReturnRequest request) {
        ModuleAssignment assignment = assignmentRepo.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        if ("returned".equals(assignment.getStatus())) {
            throw new RuntimeException("Assignment #" + assignmentId + " has already been returned");
        }

        boolean hasSkuLines = assignment.getSkuLines() != null && !assignment.getSkuLines().isEmpty();

        if (hasSkuLines) {
            returnWithSkuLines(assignment, request);
        } else {
            int returnedOk = request.getReturnedOk() != null ? request.getReturnedOk() : 0;
            int damaged = request.getDamaged() != null ? request.getDamaged() : 0;
            int missing = request.getMissing() != null ? request.getMissing() : 0;
            int total = returnedOk + damaged + missing;
            if (total != assignment.getQuantitySent()) {
                throw new RuntimeException(
                        "Reconciliation failed: returned(" + returnedOk + ") + damaged(" + damaged
                        + ") + missing(" + missing + ") = " + total
                        + " but quantity sent was " + assignment.getQuantitySent());
            }
            assignment.setQuantityReturnedOk(returnedOk);
            assignment.setQuantityDamaged(damaged);
            assignment.setQuantityMissing(missing);
        }

        assignment.setStatus("returned");
        assignment.setCompletionDate(LocalDateTime.now());
        assignmentRepo.save(assignment);

        ProductionBatch batch = assignment.getBatch();
        Long designId = batch.getSuit().getDesign().getDesignId();
        ProductionStage returnedStage = assignment.getModule().getStage();
        boolean updatesInventory = isInventoryReturnStage(returnedStage, designId);

        if (updatesInventory) {
            applyFinalStageInventory(assignment, batch);
            batch.setTotalSuitProduced(sumInventoryStageReturns(batch, designId));
        }

        if (updatesInventory && batch.getTotalSuitProduced() >= batch.getTotalSuitPlanned()
                && !hasPendingPieces(batch)) {
            batch.setStatus("completed");
            batch.setEndDate(java.time.LocalDate.now());
        } else if ("planned".equals(batch.getStatus())) {
            batch.setStatus("in_progress");
        }

        batchRepo.save(batch);

        if (isCuttingStitchingStage(returnedStage)) {
            autoForwardToPressAndPacking(assignment, batch, designId);
        }

        return assignmentRepo.findByIdWithDetails(assignmentId).orElse(assignment);
    }

    private void autoForwardToPressAndPacking(ModuleAssignment cuttingAssignment,
                                              ProductionBatch batch,
                                              Long designId) {
        ProductionStage nextStage = stagePathService.getNextStage(designId, cuttingAssignment.getModule().getStage().getStageId());
        if (nextStage == null || !isPressAndPackingStage(nextStage)) {
            return;
        }

        int returnedOk = cuttingAssignment.getQuantityReturnedOk() != null
                ? cuttingAssignment.getQuantityReturnedOk() : 0;
        if (returnedOk <= 0) {
            return;
        }

        List<ProductionModule> modules = moduleRepo.findByStageAndStatusOrderByModuleIdAsc(nextStage, "active");
        if (modules.isEmpty()) {
            throw new RuntimeException("No active module found for stage: " + nextStage.getStageName());
        }

        Supervisor packingSupervisor = productionSettingsService.requirePackingSupervisor();
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        if (cuttingAssignment.getDueDate() != null
                && cuttingAssignment.getDueDate().isAfter(LocalDateTime.now())) {
            dueDate = cuttingAssignment.getDueDate();
        }

        ModuleAssignment forward = ModuleAssignment.builder()
                .batch(batch)
                .module(modules.get(0))
                .supervisor(packingSupervisor)
                .dueDate(dueDate)
                .build();

        if (cuttingAssignment.getSkuLines() != null && !cuttingAssignment.getSkuLines().isEmpty()) {
            List<ModuleAssignmentSkuLine> skuLines = new ArrayList<>();
            int lineTotal = 0;
            for (ModuleAssignmentSkuLine line : cuttingAssignment.getSkuLines()) {
                int ok = line.getQuantityReturnedOk() != null ? line.getQuantityReturnedOk() : 0;
                if (ok <= 0) continue;
                skuLines.add(ModuleAssignmentSkuLine.builder()
                        .assignment(forward)
                        .size(line.getSize())
                        .color(line.getColor())
                        .quantitySent(ok)
                        .build());
                lineTotal += ok;
            }
            if (skuLines.isEmpty()) {
                return;
            }
            forward.setSkuLines(skuLines);
            forward.setQuantitySent(lineTotal);
        } else {
            forward.setQuantitySent(returnedOk);
        }

        dispatchInternal(forward);
    }

    private void returnWithSkuLines(ModuleAssignment assignment, ReturnRequest request) {
        if (request.getSkuLines() == null || request.getSkuLines().isEmpty()) {
            throw new RuntimeException("Per-line return counts are required for size/color dispatches");
        }

        int totalOk = 0;
        int totalDamaged = 0;
        int totalMissing = 0;

        for (ReturnRequest.SkuLineReturnRequest lineReq : request.getSkuLines()) {
            ModuleAssignmentSkuLine line = assignment.getSkuLines().stream()
                    .filter(l -> Objects.equals(l.getLineId(), lineReq.getLineId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("SKU line not found: " + lineReq.getLineId()));

            int ok = lineReq.getReturnedOk() != null ? lineReq.getReturnedOk() : 0;
            int damaged = lineReq.getDamaged() != null ? lineReq.getDamaged() : 0;
            int missing = lineReq.getMissing() != null ? lineReq.getMissing() : 0;
            int lineTotal = ok + damaged + missing;

            if (lineTotal != line.getQuantitySent()) {
                throw new RuntimeException(
                        "Line " + line.getSize().getSizeValue() + " / " + line.getColor()
                        + ": returned + damaged + missing must equal " + line.getQuantitySent());
            }

            line.setQuantityReturnedOk(ok);
            line.setQuantityDamaged(damaged);
            line.setQuantityMissing(missing);
            totalOk += ok;
            totalDamaged += damaged;
            totalMissing += missing;
        }

        assignment.setQuantityReturnedOk(totalOk);
        assignment.setQuantityDamaged(totalDamaged);
        assignment.setQuantityMissing(totalMissing);
    }

    private boolean hasPressAndPackingInPath(Long designId) {
        return stagePathService.getRequiredStagesForDesign(designId).stream()
                .anyMatch(this::isPressAndPackingStage);
    }

    private ProductionStage resolveInventoryStage(Long designId) {
        return stagePathService.getRequiredStagesForDesign(designId).stream()
                .filter(this::isPressAndPackingStage)
                .findFirst()
                .orElseGet(() -> stagePathService.getFinalStage(designId));
    }

    /** Inventory updates only when the inventory stage is returned (Press and Packing when in path). */
    private boolean isInventoryReturnStage(ProductionStage returnedStage, Long designId) {
        if (hasPressAndPackingInPath(designId)) {
            return isPressAndPackingStage(returnedStage);
        }
        ProductionStage finalStage = stagePathService.getFinalStage(designId);
        return returnedStage.getStageId().equals(finalStage.getStageId());
    }

    private void applyFinalStageInventory(ModuleAssignment assignment, ProductionBatch batch) {
        Design design = batch.getSuit().getDesign();
        Long designId = design.getDesignId();

        if (assignment.getSkuLines() != null && !assignment.getSkuLines().isEmpty()) {
            int producedAfterReturn = sumInventoryStageReturns(batch, designId);
            if (producedAfterReturn > batch.getTotalSuitPlanned()) {
                throw new RuntimeException(
                        "Final stage returns would exceed batch planned quantity "
                        + batch.getTotalSuitPlanned());
            }
            for (ModuleAssignmentSkuLine line : assignment.getSkuLines()) {
                if (line.getQuantityReturnedOk() > 0) {
                    Suit suit = findOrCreateSuit(design, line.getSize(), line.getColor());
                    inventoryService.addStock(suit, line.getQuantityReturnedOk());
                }
            }
        } else if (assignment.getQuantityReturnedOk() > 0) {
            int producedAfterReturn = sumInventoryStageReturns(batch, designId);
            if (producedAfterReturn > batch.getTotalSuitPlanned()) {
                throw new RuntimeException(
                        "Cannot record " + assignment.getQuantityReturnedOk()
                        + " OK pieces — batch planned " + batch.getTotalSuitPlanned());
            }
            inventoryService.addStock(batch.getSuit(), assignment.getQuantityReturnedOk());
        }
    }

    private Suit findOrCreateSuit(Design design, Size size, String color) {
        return suitRepo.findByDesignAndSizeAndColor(design, size, color)
                .orElseGet(() -> suitRepo.save(Suit.builder()
                        .design(design)
                        .size(size)
                        .color(color)
                        .status("active")
                        .build()));
    }

    private boolean isDesigningStage(ProductionStage stage) {
        return "Designing".equalsIgnoreCase(stage.getStageName());
    }

    private boolean isFillingStage(ProductionStage stage) {
        return "Filling".equalsIgnoreCase(stage.getStageName());
    }

    private boolean isCuttingStitchingStage(ProductionStage stage) {
        if (stage == null) return false;
        String name = stage.getStageName();
        return "Cutting & Stitching".equalsIgnoreCase(name)
                || "Cutting".equalsIgnoreCase(name)
                || "Stitching".equalsIgnoreCase(name);
    }

    private boolean isPressAndPackingStage(ProductionStage stage) {
        return stage != null && "Press and Packing".equalsIgnoreCase(stage.getStageName());
    }

    private boolean requiresSkuBreakdown(ProductionStage stage) {
        return isCuttingStitchingStage(stage);
    }

    private boolean hasPendingPieces(ProductionBatch batch) {
        return assignmentRepo.findByBatch(batch).stream()
                .anyMatch(a -> {
                    if (!"returned".equals(a.getStatus())) return true;
                    int accounted = a.getQuantityReturnedOk() + a.getQuantityDamaged() + a.getQuantityMissing();
                    return accounted < a.getQuantitySent();
                });
    }

    private void validateStageDispatch(ProductionBatch batch, ProductionModule targetModule, int quantitySent) {
        Long designId = batch.getSuit().getDesign().getDesignId();
        List<ProductionStage> path = stagePathService.getRequiredStagesForDesign(designId);
        ProductionStage targetStage = targetModule.getStage();

        boolean inPath = path.stream()
                .anyMatch(s -> s.getStageId().equals(targetStage.getStageId()));
        if (!inPath) {
            throw new RuntimeException("Stage '" + targetStage.getStageName()
                    + "' is not in the production path for this design");
        }

        int targetOrder = targetStage.getStageOrder();
        ProductionStage firstStage = path.get(0);

        if (targetStage.getStageId().equals(firstStage.getStageId())) {
            int sentToFirst = sumSentToStageOrder(batch, firstStage.getStageOrder());
            int max = batch.getTotalSuitPlanned() - sentToFirst;
            if (quantitySent > max) {
                throw new RuntimeException("Cannot dispatch " + quantitySent
                        + " to " + targetStage.getStageName() + ". Maximum available: " + max);
            }
            return;
        }

        ProductionStage previousStage = null;
        for (int i = 1; i < path.size(); i++) {
            if (path.get(i).getStageId().equals(targetStage.getStageId())) {
                previousStage = path.get(i - 1);
                break;
            }
        }
        if (previousStage == null) {
            throw new RuntimeException("Invalid stage sequence for design");
        }

        int returnedFromPrev = sumReturnedOkFromStageOrder(batch, previousStage.getStageOrder());
        int alreadyForwarded = sumSentToStageOrderAtOrAfter(batch, targetOrder);
        int available = returnedFromPrev - alreadyForwarded;

        if (quantitySent > available) {
            throw new RuntimeException("Cannot dispatch " + quantitySent + " to "
                    + targetStage.getStageName() + ". Only " + available
                    + " pieces returned OK from " + previousStage.getStageName()
                    + " are available to forward (partial forwarding allowed)");
        }
    }

    private int sumSentToStageOrder(ProductionBatch batch, int stageOrder) {
        return assignmentRepo.findByBatch(batch).stream()
                .filter(a -> a.getModule().getStage().getStageOrder().equals(stageOrder))
                .mapToInt(ModuleAssignment::getQuantitySent)
                .sum();
    }

    private int sumSentToStageOrderAtOrAfter(ProductionBatch batch, int stageOrder) {
        return assignmentRepo.findByBatch(batch).stream()
                .filter(a -> a.getModule().getStage().getStageOrder() >= stageOrder)
                .mapToInt(ModuleAssignment::getQuantitySent)
                .sum();
    }

    private int sumReturnedOkFromStageOrder(ProductionBatch batch, int stageOrder) {
        return assignmentRepo.findByBatch(batch).stream()
                .filter(a -> a.getModule().getStage().getStageOrder().equals(stageOrder))
                .filter(a -> "returned".equals(a.getStatus()))
                .mapToInt(ModuleAssignment::getQuantityReturnedOk)
                .sum();
    }

    private int sumInventoryStageReturns(ProductionBatch batch, Long designId) {
        ProductionStage inventoryStage = resolveInventoryStage(designId);
        Long inventoryStageId = inventoryStage.getStageId();
        return assignmentRepo.findByBatch(batch).stream()
                .filter(a -> a.getModule().getStage().getStageId().equals(inventoryStageId))
                .filter(a -> "returned".equals(a.getStatus()))
                .mapToInt(a -> {
                    if (a.getSkuLines() != null && !a.getSkuLines().isEmpty()) {
                        return a.getSkuLines().stream()
                                .mapToInt(ModuleAssignmentSkuLine::getQuantityReturnedOk)
                                .sum();
                    }
                    return a.getQuantityReturnedOk();
                })
                .sum();
    }

    public boolean areDependenciesMet(Long assignmentId) {
        ModuleAssignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));
        List<ModuleAssignmentDependency> dependencies = dependencyRepo.findByAssignment(assignment);
        return dependencies.stream()
                .allMatch(dep -> "returned".equals(dep.getDependsOnAssignment().getStatus()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleAssignment> getAll() {
        return assignmentRepo.findAllWithDetails();
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleAssignment getById(Long id) {
        return assignmentRepo.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleAssignment> getByBatchId(Long batchId) {
        ProductionBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));
        return assignmentRepo.findByBatch(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleAssignment> getBySupervisorId(Long supervisorId) {
        return assignmentRepo.findBySupervisorWithDetails(supervisorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleAssignment> getMineForCurrentSupervisor() {
        Long supervisorId = currentUserService.requireSupervisorId();
        return getBySupervisorId(supervisorId);
    }

    @Override
    public List<ModuleAssignment> getOverdue() {
        return assignmentRepo.findOverdueAssignments(LocalDateTime.now());
    }
}
