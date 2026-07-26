package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.ProductionSettingsDTO;
import com.dreams.dreamscreations.dto.UpdateProductionSettingsRequest;
import com.dreams.dreamscreations.entity.Supervisor;
import com.dreams.dreamscreations.entity.SystemSetting;
import com.dreams.dreamscreations.repository.SupervisorRepository;
import com.dreams.dreamscreations.repository.SystemSettingRepository;
import com.dreams.dreamscreations.service.ProductionSettingsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionSettingsServiceImpl implements ProductionSettingsService {

    public static final String PACKING_SUPERVISOR_KEY = "packing_supervisor_id";

    private final SystemSettingRepository settingRepo;
    private final SupervisorRepository supervisorRepo;
    private final String defaultPackingSupervisorName;

    public ProductionSettingsServiceImpl(SystemSettingRepository settingRepo,
                                         SupervisorRepository supervisorRepo,
                                         @Value("${app.production.default-packing-supervisor-name:Asif}")
                                         String defaultPackingSupervisorName) {
        this.settingRepo = settingRepo;
        this.supervisorRepo = supervisorRepo;
        this.defaultPackingSupervisorName = defaultPackingSupervisorName;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionSettingsDTO getSettings() {
        Supervisor supervisor = resolvePackingSupervisor();
        return toDto(supervisor);
    }

    @Override
    @Transactional
    public ProductionSettingsDTO updateSettings(UpdateProductionSettingsRequest request) {
        if (request.getPackingSupervisorId() == null) {
            throw new RuntimeException("Packing supervisor is required");
        }
        Supervisor supervisor = supervisorRepo.findById(request.getPackingSupervisorId())
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));
        if (!"active".equalsIgnoreCase(supervisor.getStatus())) {
            throw new RuntimeException("Packing supervisor must be an active supervisor");
        }
        saveSetting(PACKING_SUPERVISOR_KEY, String.valueOf(supervisor.getSupervisorId()));
        return toDto(supervisor);
    }

    @Override
    @Transactional(readOnly = true)
    public Supervisor requirePackingSupervisor() {
        Supervisor supervisor = resolvePackingSupervisor();
        if (supervisor == null) {
            throw new RuntimeException(
                    "Packing supervisor is not configured. Set it in Factory Setup → Production Settings.");
        }
        return supervisor;
    }

    private Supervisor resolvePackingSupervisor() {
        return settingRepo.findById(PACKING_SUPERVISOR_KEY)
                .map(s -> {
                    try {
                        Long id = Long.parseLong(s.getSettingValue());
                        return supervisorRepo.findById(id).orElse(null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .orElseGet(this::bootstrapDefaultSupervisor);
    }

    private Supervisor bootstrapDefaultSupervisor() {
        if (defaultPackingSupervisorName == null || defaultPackingSupervisorName.isBlank()) {
            return null;
        }
        return supervisorRepo.findAll().stream()
                .filter(s -> defaultPackingSupervisorName.equalsIgnoreCase(s.getFirstName()))
                .findFirst()
                .map(supervisor -> {
                    saveSetting(PACKING_SUPERVISOR_KEY, String.valueOf(supervisor.getSupervisorId()));
                    return supervisor;
                })
                .orElse(null);
    }

    private void saveSetting(String key, String value) {
        settingRepo.save(SystemSetting.builder()
                .settingKey(key)
                .settingValue(value)
                .build());
    }

    private ProductionSettingsDTO toDto(Supervisor supervisor) {
        if (supervisor == null) {
            return new ProductionSettingsDTO(null, null);
        }
        String name = supervisor.getFirstName()
                + (supervisor.getLastName() != null ? " " + supervisor.getLastName() : "");
        return new ProductionSettingsDTO(supervisor.getSupervisorId(), name.trim());
    }
}
