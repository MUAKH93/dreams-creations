package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.*;

import java.time.LocalDate;
import java.util.List;

public interface FinanceBankService {

    List<FinanceBankAccountDTO> getBankAccounts(boolean activeOnly);

    FinanceBankAccountDTO createBankAccount(CreateFinanceBankAccountRequest request);

    List<FinanceBankTransactionDTO> getTransactions(Long bankAccountId);

    FinanceBankTransactionDTO createTransaction(CreateFinanceBankTransactionRequest request);

    FinanceBankTransactionDTO reconcileTransaction(Long transactionId, ReconcileBankTransactionRequest request);

    BankReconciliationReportDTO getReconciliation(Long bankAccountId, LocalDate asOfDate);
}
