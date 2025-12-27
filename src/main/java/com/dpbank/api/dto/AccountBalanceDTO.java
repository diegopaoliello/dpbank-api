package com.dpbank.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "AccountBalance", description = "Representacao do saldo atual da conta")
public record AccountBalanceDTO(
        @Schema(description = "Identificador unico da conta", example = "2d2f3d02-3ec6-4e5b-8d2a-5a497c2a5db7")
        UUID accountId,
        @Schema(description = "Numero legivel da conta", example = "1234567890")
        String numeroConta,
        @Schema(description = "Saldo consolidado da conta", example = "998.25")
        BigDecimal saldo
) { }
