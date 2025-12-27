package com.dpbank.api;

import com.dpbank.api.domain.Account;
import com.dpbank.api.domain.TransactionType;
import com.dpbank.api.dto.TransactionRequestDTO;
import com.dpbank.api.repository.AccountRepository;
import com.dpbank.api.repository.TransactionRepository;
import com.dpbank.api.service.AccountService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ConcurrencyIntegrationTest extends AbstractIntegrationTest {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");
    private static final BigDecimal DEBIT_VALUE = new BigDecimal("100.00");
    private static final int THREADS = 10;

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
    void deveManterSaldoConsistenteComDebitosConcorrentes() throws Exception {
        Account account = accountRepository.save(Account.builder()
                .numeroConta("ACC-CONC-001")
                .saldo(INITIAL_BALANCE)
                .build());

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < THREADS; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    accountService.processarLancamentos(account.getId(),
                            List.of(new TransactionRequestDTO(DEBIT_VALUE, TransactionType.DEBITO)));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Thread interrompida", e);
                }
            }));
        }

        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        Account updated = accountRepository.findById(account.getId()).orElseThrow();
        Assertions.assertThat(updated.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
