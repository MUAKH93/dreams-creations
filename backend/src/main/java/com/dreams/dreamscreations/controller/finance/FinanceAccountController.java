package com.dreams.dreamscreations.controller.finance;

import com.dreams.dreamscreations.dto.finance.CreateFinanceAccountRequest;
import com.dreams.dreamscreations.dto.finance.FinanceAccountDTO;
import com.dreams.dreamscreations.dto.finance.UpdateFinanceAccountRequest;
import com.dreams.dreamscreations.service.finance.FinanceAccountService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/accounts")
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceAccountController {

    private final FinanceAccountService accountService;

    public FinanceAccountController(FinanceAccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<FinanceAccountDTO>> getAll(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(accountService.getAll(activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinanceAccountDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getById(id));
    }

    @PostMapping
    public ResponseEntity<FinanceAccountDTO> create(@RequestBody CreateFinanceAccountRequest request) {
        return ResponseEntity.ok(accountService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinanceAccountDTO> update(@PathVariable Long id,
                                                    @RequestBody UpdateFinanceAccountRequest request) {
        return ResponseEntity.ok(accountService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
