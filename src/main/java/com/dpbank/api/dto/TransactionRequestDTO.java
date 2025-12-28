package com.dpbank.api.dto;

import com.dpbank.api.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Input payload used to send debit/credit launches (requirement 1 and 2).
 */
@Schema(name = "TransactionRequest", description = "Single debit/credit entry applied to an account")
public record TransactionRequestDTO(
        @Schema(description = "Monetary amount to be applied", example = "150.75")
        @NotNull(message = "{transaction.amount.required}")
        @Positive(message = "{transaction.amount.positive}")
        BigDecimal amount,
        @Schema(description = "Transaction type. Use CREDIT to add funds or DEBIT to remove them", example = "DEBIT")
        @NotNull(message = "{transaction.type.required}")
        TransactionType type
) { }
