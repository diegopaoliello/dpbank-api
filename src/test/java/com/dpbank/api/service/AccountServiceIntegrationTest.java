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
import java.util.UUID;
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
    @DisplayName("Deve aplicar sequencia de creditos e debitos garantindo saldo correto")
    void deveProcessarCreditosEDebitos() {
        Account account = criarConta("ACC-SVC-001", new BigDecimal("500.00"));

        List<TransactionRequestDTO> lancamentos = List.of(
                new TransactionRequestDTO(new BigDecimal("200.00"), TransactionType.CREDITO),
                new TransactionRequestDTO(new BigDecimal("150.00"), TransactionType.DEBITO),
                new TransactionRequestDTO(new BigDecimal("25.50"), TransactionType.CREDITO)
        );

        Account atualizado = accountService.processarLancamentos(account.getId(), lancamentos);

        Assertions.assertThat(atualizado.getSaldo()).isEqualByComparingTo(new BigDecimal("575.50"));
        Assertions.assertThat(transactionRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve lançar excecao ao tentar debitar acima do saldo")
    void deveLancarExcecaoAoDebitarSemSaldo() {
        Account account = criarConta("ACC-SVC-002", new BigDecimal("50.00"));

        List<TransactionRequestDTO> lancamentos = List.of(
                new TransactionRequestDTO(new BigDecimal("100.00"), TransactionType.DEBITO)
        );

        Assertions.assertThatThrownBy(() -> accountService.processarLancamentos(account.getId(), lancamentos))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    private Account criarConta(String numeroConta, BigDecimal saldo) {
        return accountRepository.save(Account.builder()
                .numeroConta(numeroConta)
                .saldo(saldo)
                .build());
    }
}
