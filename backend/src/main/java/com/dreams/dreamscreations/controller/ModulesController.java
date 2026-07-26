package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.config.ModuleProperties;
import com.dreams.dreamscreations.dto.ModuleFlagsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/modules")
public class ModulesController {

    private final ModuleProperties moduleProperties;

    public ModulesController(ModuleProperties moduleProperties) {
        this.moduleProperties = moduleProperties;
    }

    @GetMapping
    public ResponseEntity<ModuleFlagsDTO> getModuleFlags() {
        ModuleFlagsDTO flags = new ModuleFlagsDTO(
                new ModuleFlagsDTO.ModuleInfo(moduleProperties.getFinance().isEnabled()),
                new ModuleFlagsDTO.ModuleInfo(moduleProperties.getShop().isEnabled())
        );
        return ResponseEntity.ok(flags);
    }
}
