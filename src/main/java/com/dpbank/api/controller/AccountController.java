package com.dpbank.api.controller;

import com.dpbank.api.domain.Account;
import com.dpbank.api.dto.AccountBalanceDTO;
import com.dpbank.api.dto.TransactionRequestDTO;
import com.dpbank.api.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints that fulfill requirements 1 to 5 of the assessment: execute debits/credits,
 * allow batch launches, expose balance retrieval, and document the contract via Springdoc.
 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Accounts", description = "Operations to process debits/credits and retrieve balances")
public class AccountController {

    private final AccountService accountService;

        /**
         * Applies multiple debit/credit transactions to an account.
         */
    @PostMapping("/{id}/transactions")
    @Operation(
            summary = "Process debit/credit transactions",
            description = "Applies a list of transactions to the given account using pessimistic locking to ensure consistency",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transactions processed successfully",
                            content = @Content(schema = @Schema(implementation = AccountBalanceDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
                    @ApiResponse(responseCode = "422", description = "Insufficient balance", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Invalid payload", content = @Content)
            }
    )
    public ResponseEntity<AccountBalanceDTO> processTransactions(
            @Parameter(description = "Account identifier", required = true)
            @PathVariable("id") UUID accountId,
            @RequestBody
            @Valid
            @NotEmpty(message = "{transactions.list.required}")
            List<@Valid TransactionRequestDTO> transactions) {

                int totalTransactions = transactions != null ? transactions.size() : 0;
                log.info("Processing {} transaction(s) for account {}", totalTransactions, accountId);
                Account account = accountService.processTransactions(accountId, transactions);
                log.info("Transactions processed for account {}. New balance: {}", accountId, account.getBalance());
                return ResponseEntity.ok(mapToBalance(account));
    }

        /**
         * Retrieves the current consolidated balance of an account.
         */
    @GetMapping("/{id}/balance")
    @Operation(
            summary = "Fetch current account balance",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Balance returned",
                            content = @Content(schema = @Schema(implementation = AccountBalanceDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
            }
    )
    public ResponseEntity<AccountBalanceDTO> getBalance(
            @Parameter(description = "Account identifier", required = true)
            @PathVariable("id") UUID accountId) {

                log.info("Retrieving balance for account {}", accountId);
                Account account = accountService.findAccount(accountId);
                return ResponseEntity.ok(mapToBalance(account));
    }

    private AccountBalanceDTO mapToBalance(Account account) {
        return new AccountBalanceDTO(account.getId(), account.getAccountNumber(), account.getBalance());
    }
}
