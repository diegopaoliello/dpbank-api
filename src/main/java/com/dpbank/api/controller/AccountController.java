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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Accounts", description = "Operacoes de lancamento e consulta de saldo")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/{id}/transactions")
    @Operation(
            summary = "Processa lancamentos de debito/credito",
            description = "Aplica uma lista de lancamentos na conta informada garantindo consistencia via lock pessimista",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lancamentos aplicados com sucesso",
                            content = @Content(schema = @Schema(implementation = AccountBalanceDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Conta nao encontrada", content = @Content),
                    @ApiResponse(responseCode = "422", description = "Saldo insuficiente", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Payload invalido", content = @Content)
            }
    )
    public ResponseEntity<AccountBalanceDTO> processarLancamentos(
            @Parameter(description = "Identificador da conta", required = true)
            @PathVariable("id") UUID accountId,
            @RequestBody
            @Valid
            @NotEmpty(message = "Lista de lancamentos obrigatoria")
            List<@Valid TransactionRequestDTO> lancamentos) {

        Account account = accountService.processarLancamentos(accountId, lancamentos);
        return ResponseEntity.ok(mapToBalance(account));
    }

    @GetMapping("/{id}/balance")
    @Operation(
            summary = "Consulta saldo da conta",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Saldo retornado",
                            content = @Content(schema = @Schema(implementation = AccountBalanceDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Conta nao encontrada", content = @Content)
            }
    )
    public ResponseEntity<AccountBalanceDTO> consultarSaldo(
            @Parameter(description = "Identificador da conta", required = true)
            @PathVariable("id") UUID accountId) {

        Account account = accountService.consultarConta(accountId);
        return ResponseEntity.ok(mapToBalance(account));
    }

    private AccountBalanceDTO mapToBalance(Account account) {
        return new AccountBalanceDTO(account.getId(), account.getNumeroConta(), account.getSaldo());
    }
}
