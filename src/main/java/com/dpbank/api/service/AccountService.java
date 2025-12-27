package com.dpbank.api.service;

import com.dpbank.api.domain.Account;
import com.dpbank.api.domain.Transaction;
import com.dpbank.api.domain.TransactionType;
import com.dpbank.api.dto.TransactionRequestDTO;
import com.dpbank.api.repository.AccountRepository;
import com.dpbank.api.repository.TransactionRepository;
import com.dpbank.api.service.exception.InsufficientBalanceException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Account processarLancamentos(UUID accountId, List<TransactionRequestDTO> lancamentos) {
        Objects.requireNonNull(accountId, "accountId obrigatorio");

        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Conta nao encontrada: " + accountId));

        if (lancamentos == null || lancamentos.isEmpty()) {
            return account;
        }

        for (TransactionRequestDTO dto : lancamentos) {
            validarDto(dto);
            aplicarLancamento(account, dto);
        }

        return accountRepository.save(account);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Account consultarConta(UUID accountId) {
        Objects.requireNonNull(accountId, "accountId obrigatorio");
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Conta nao encontrada: " + accountId));
    }

    private void validarDto(TransactionRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Lancamento nao pode ser nulo");
        }
        if (dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do lancamento deve ser positivo");
        }
        if (dto.tipo() == null) {
            throw new IllegalArgumentException("Tipo do lancamento obrigatorio");
        }
    }

    private void aplicarLancamento(Account account, TransactionRequestDTO dto) {
        TransactionType tipo = dto.tipo();
        BigDecimal amount = dto.valor();

        if (tipo == TransactionType.DEBITO) {
            debitar(account, amount);
        } else {
            creditar(account, amount);
        }

        Transaction transaction = Transaction.builder()
                .account(account)
                .valor(amount)
                .tipo(tipo)
                .build();

        transactionRepository.save(transaction);
    }

    private void debitar(Account account, BigDecimal amount) {
        if (account.getSaldo().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Saldo insuficiente para conta " + account.getNumeroConta());
        }
        account.setSaldo(account.getSaldo().subtract(amount));
    }

    private void creditar(Account account, BigDecimal amount) {
        account.setSaldo(account.getSaldo().add(amount));
    }
}
