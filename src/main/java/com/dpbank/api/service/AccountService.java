package com.dpbank.api.service;

import com.dpbank.api.domain.Account;
import com.dpbank.api.domain.Transaction;
import com.dpbank.api.domain.TransactionType;
import com.dpbank.api.dto.TransactionRequestDTO;
import com.dpbank.api.repository.AccountRepository;
import com.dpbank.api.repository.TransactionRepository;
import com.dpbank.api.service.exception.AccountNotFoundException;
import com.dpbank.api.service.exception.InsufficientBalanceException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Core business rules that satisfy requirements 1 to 4 and 7: transactional processing,
 * concurrency control, data consistency, and use of Spring + Hibernate stack.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Applies the provided debits/credits to the account identified by {@code accountId}.
     */
    @Transactional
    public Account processTransactions(UUID accountId, List<TransactionRequestDTO> transactions) {
        Objects.requireNonNull(accountId, "accountId is required");

        // Requirements 3 and 4: pessimistic locking prevents race conditions while processing concurrent launches.
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (transactions == null || transactions.isEmpty()) {
            log.info("Received empty transaction list for account {}. Skipping processing.", accountId);
            return account;
        }

        log.info("Applying {} transaction(s) to account {}", transactions.size(), accountId);
        for (TransactionRequestDTO transaction : transactions) {
            applyTransaction(account, transaction);
        }

        Account persisted = accountRepository.save(account);
        log.info("Account {} persisted with balance {}", accountId, persisted.getBalance());
        return persisted;
    }

    /**
     * Fetches the account information without mutating state.
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public Account findAccount(UUID accountId) {
        Objects.requireNonNull(accountId, "accountId is required");
        log.info("Looking up account {}", accountId);
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    /**
     * Maps the incoming DTO into a persisted transaction and updates the in-memory aggregate.
     */
    private void applyTransaction(Account account, TransactionRequestDTO dto) {
        TransactionType type = dto.type();
        BigDecimal amount = dto.amount();

        log.debug("Applying {} of {} to account {}", type, amount, account.getId());

        if (type == TransactionType.DEBIT) {
            debit(account, amount);
        } else {
            credit(account, amount);
        }

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(amount)
                .type(type)
                .build();

        transactionRepository.save(transaction);
    }

    /**
     * Validates and subtracts the requested amount from the balance.
     */
    private void debit(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance for account {} when debiting {}", account.getAccountNumber(), amount);
            throw new InsufficientBalanceException("error.insufficientBalance.detail", account.getAccountNumber());
        }
        account.setBalance(account.getBalance().subtract(amount));
        log.debug("Debited {} from account {}. Balance is now {}", amount, account.getId(), account.getBalance());
    }

    /**
     * Adds the requested amount to the balance.
     */
    private void credit(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        log.debug("Credited {} to account {}. Balance is now {}", amount, account.getId(), account.getBalance());
    }
}
