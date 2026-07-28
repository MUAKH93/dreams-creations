package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.config.ModuleProperties;
import com.dreams.dreamscreations.dto.finance.FinanceModuleStatusDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceModuleServiceImpl implements FinanceModuleService {

    private final ModuleProperties moduleProperties;

    public FinanceModuleServiceImpl(ModuleProperties moduleProperties) {
        this.moduleProperties = moduleProperties;
    }

    @Override
    public FinanceModuleStatusDTO getStatus() {
        FinanceModuleStatusDTO status = new FinanceModuleStatusDTO();
        status.setCurrentPhase("F6");
        status.setCompletedPhases(List.of(
                "Scaffold", "F1 — Core ledger", "F2 — Accounts receivable",
                "F3 — Inventory & COGS", "F4 — Financial statements", "F5 — Payables",
                "F6 — Bank reconciliation"));
        status.setUpcomingPhases(List.of(
                "UAT sign-off & merge to main"
        ));
        status.setAutoPostAr(moduleProperties.getFinance().isAutoPostAr());
        status.setAutoPostInventory(moduleProperties.getFinance().isAutoPostInventory());
        status.setAutoPostAp(moduleProperties.getFinance().isAutoPostAp());
        status.setMessage(
                "Phase F6 active: bank accounts, statement lines, and book-vs-bank reconciliation."
        );
        return status;
    }
}
