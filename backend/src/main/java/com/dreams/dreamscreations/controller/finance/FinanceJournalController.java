package com.dreams.dreamscreations.controller.finance;

import com.dreams.dreamscreations.dto.finance.CreateFinanceJournalRequest;
import com.dreams.dreamscreations.dto.finance.FinanceJournalEntryDTO;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.finance.FinanceJournalService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/journals")
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceJournalController {

    private final FinanceJournalService journalService;
    private final CurrentUserService currentUserService;

    public FinanceJournalController(FinanceJournalService journalService,
                                      CurrentUserService currentUserService) {
        this.journalService = journalService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<FinanceJournalEntryDTO>> getAll() {
        return ResponseEntity.ok(journalService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinanceJournalEntryDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(journalService.getById(id));
    }

    @PostMapping
    public ResponseEntity<FinanceJournalEntryDTO> create(@RequestBody CreateFinanceJournalRequest request) {
        Long userId = currentUserService.getCurrentUser().getUserId();
        return ResponseEntity.ok(journalService.createManualEntry(request, userId));
    }
}
