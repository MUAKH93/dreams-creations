package com.dreams.dreamscreations.controller.finance;

import com.dreams.dreamscreations.dto.finance.FinanceModuleStatusDTO;
import com.dreams.dreamscreations.service.finance.FinanceModuleService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Finance module API root. Additional controllers (accounts, journals, reports)
 * will be added under /api/finance/** in Phase F1+.
 */
@RestController
@RequestMapping("/api/finance")
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceStatusController {

    private final FinanceModuleService financeModuleService;

    public FinanceStatusController(FinanceModuleService financeModuleService) {
        this.financeModuleService = financeModuleService;
    }

    @GetMapping("/status")
    public ResponseEntity<FinanceModuleStatusDTO> status() {
        return ResponseEntity.ok(financeModuleService.getStatus());
    }
}
