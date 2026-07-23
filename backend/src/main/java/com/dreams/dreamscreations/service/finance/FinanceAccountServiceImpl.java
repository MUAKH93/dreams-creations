package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.*;
import com.dreams.dreamscreations.entity.finance.FinanceAccount;
import com.dreams.dreamscreations.repository.finance.FinanceAccountRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "modules.finance.enabled", havingValue = "true")
public class FinanceAccountServiceImpl implements FinanceAccountService {

    private static final Set<String> ACCOUNT_TYPES =
            Set.of("ASSET", "LIABILITY", "EQUITY", "INCOME", "EXPENSE");

    private final FinanceAccountRepository accountRepo;

    public FinanceAccountServiceImpl(FinanceAccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceAccountDTO> getAll(boolean activeOnly) {
        List<FinanceAccount> accounts = activeOnly
                ? accountRepo.findByIsActiveTrueOrderByAccountCodeAsc()
                : accountRepo.findAllByOrderByAccountCodeAsc();
        return accounts.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceAccountDTO getById(Long id) {
        return toDto(requireAccount(id));
    }

    @Override
    @Transactional
    public FinanceAccountDTO create(CreateFinanceAccountRequest request) {
        validateAccountType(request.getAccountType());
        if (request.getAccountCode() == null || request.getAccountCode().isBlank()) {
            throw new RuntimeException("Account code is required");
        }
        if (request.getAccountName() == null || request.getAccountName().isBlank()) {
            throw new RuntimeException("Account name is required");
        }
        String code = request.getAccountCode().trim();
        if (accountRepo.existsByAccountCode(code)) {
            throw new RuntimeException("Account code already exists: " + code);
        }

        FinanceAccount parent = resolveParent(request.getParentId(), null);
        FinanceAccount saved = accountRepo.save(FinanceAccount.builder()
                .accountCode(code)
                .accountName(request.getAccountName().trim())
                .accountType(request.getAccountType().trim().toUpperCase())
                .parent(parent)
                .description(request.getDescription())
                .isActive(true)
                .isSystem(false)
                .build());
        return toDto(saved);
    }

    @Override
    @Transactional
    public FinanceAccountDTO update(Long id, UpdateFinanceAccountRequest request) {
        FinanceAccount account = requireAccount(id);
        if (request.getAccountName() != null && !request.getAccountName().isBlank()) {
            account.setAccountName(request.getAccountName().trim());
        }
        if (request.getAccountType() != null && !request.getAccountType().isBlank()) {
            validateAccountType(request.getAccountType());
            account.setAccountType(request.getAccountType().trim().toUpperCase());
        }
        if (request.getParentId() != null) {
            account.setParent(resolveParent(request.getParentId(), id));
        }
        if (request.getIsActive() != null) {
            account.setIsActive(request.getIsActive());
        }
        if (request.getDescription() != null) {
            account.setDescription(request.getDescription());
        }
        return toDto(accountRepo.save(account));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        FinanceAccount account = requireAccount(id);
        if (Boolean.TRUE.equals(account.getIsSystem())) {
            throw new RuntimeException("System accounts cannot be deleted");
        }
        if (accountRepo.hasJournalLines(id)) {
            throw new RuntimeException("Cannot delete account with journal activity");
        }
        accountRepo.delete(account);
    }

    private FinanceAccount requireAccount(Long id) {
        return accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
    }

    private FinanceAccount resolveParent(Long parentId, Long selfId) {
        if (parentId == null) return null;
        if (selfId != null && parentId.equals(selfId)) {
            throw new RuntimeException("Account cannot be its own parent");
        }
        return requireAccount(parentId);
    }

    private void validateAccountType(String accountType) {
        if (accountType == null || !ACCOUNT_TYPES.contains(accountType.trim().toUpperCase())) {
            throw new RuntimeException("Account type must be one of: " + String.join(", ", ACCOUNT_TYPES));
        }
    }

    private FinanceAccountDTO toDto(FinanceAccount account) {
        return FinanceAccountDTO.builder()
                .accountId(account.getAccountId())
                .accountCode(account.getAccountCode())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .parentId(account.getParent() != null ? account.getParent().getAccountId() : null)
                .parentCode(account.getParent() != null ? account.getParent().getAccountCode() : null)
                .isActive(account.getIsActive())
                .isSystem(account.getIsSystem())
                .description(account.getDescription())
                .build();
    }
}
