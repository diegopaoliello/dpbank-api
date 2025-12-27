package com.dpbank.api.dto;

import com.dpbank.api.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(name = "TransactionRequest", description = "Lancamento individual aplicado a uma conta corrente")
public record TransactionRequestDTO(
        @Schema(description = "Valor monetario do lancamento", example = "150.75")
        @NotNull(message = "Valor obrigatorio")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal valor,
        @Schema(description = "Tipo do lancamento, CREDITO para adicionar saldo e DEBITO para subtrair", example = "DEBITO")
        @NotNull(message = "Tipo obrigatorio")
        TransactionType tipo
) { }
