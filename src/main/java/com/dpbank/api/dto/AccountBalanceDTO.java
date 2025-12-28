package com.dpbank.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Snapshot returned by the balance endpoint (requirement 2).
 */
@Schema(name = "AccountBalance", description = "Representation of the current account balance")
public record AccountBalanceDTO(
        @Schema(description = "Unique account identifier", example = "2d2f3d02-3ec6-4e5b-8d2a-5a497c2a5db7")
        UUID accountId,
        @Schema(description = "Human-readable account number", example = "1234567890")
        String accountNumber,
        @Schema(description = "Current consolidated balance", example = "998.25")
        BigDecimal balance
) { }
