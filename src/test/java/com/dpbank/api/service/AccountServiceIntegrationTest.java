package com.dpbank.api.service;

import com.dpbank.api.AbstractIntegrationTest;
import com.dpbank.api.domain.Account;
import com.dpbank.api.domain.TransactionType;
import com.dpbank.api.dto.TransactionRequestDTO;
import com.dpbank.api.repository.AccountRepository;
import com.dpbank.api.repository.TransactionRepository;
import com.dpbank.api.service.exception.InsufficientBalanceException;
import java.math.BigDecimal;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AccountServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @AfterEach
    void cleanup() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }


    @Test
    @DisplayName("Should apply credits and debits keeping the balance consistent")
    void shouldProcessDebitsAndCredits() {
        Account account = createAccount("ACC-SVC-001", new BigDecimal("500.00"));

        List<TransactionRequestDTO> transactions = List.of(
                new TransactionRequestDTO(new BigDecimal("200.00"), TransactionType.CREDIT),
                new TransactionRequestDTO(new BigDecimal("150.00"), TransactionType.DEBIT),
                new TransactionRequestDTO(new BigDecimal("25.50"), TransactionType.CREDIT)
        );

        Account updated = accountService.processTransactions(account.getId(), transactions);

        Assertions.assertThat(updated.getBalance()).isEqualByComparingTo(new BigDecimal("575.50"));
        Assertions.assertThat(transactionRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should raise an exception when debiting more than the current balance")
    void shouldFailWhenDebitingWithoutBalance() {
        Account account = createAccount("ACC-SVC-002", new BigDecimal("50.00"));

        List<TransactionRequestDTO> transactions = List.of(
                new TransactionRequestDTO(new BigDecimal("100.00"), TransactionType.DEBIT)
        );

        Assertions.assertThatThrownBy(() -> accountService.processTransactions(account.getId(), transactions))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("error.insufficientBalance.detail");
    }

    private Account createAccount(String accountNumber, BigDecimal balance) {
        return accountRepository.save(Account.builder()
                .accountNumber(accountNumber)
                .balance(balance)
                .build());
    }
}
