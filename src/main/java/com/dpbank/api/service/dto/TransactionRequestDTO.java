package com.dpbank.api.service.dto;

import com.dpbank.api.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransactionRequestDTO(
        @NotNull(message = "Valor obrigatorio")
        @DecimalMin(value = "0.01", message = "Valor deve ser positivo")
        BigDecimal valor,
        @NotNull(message = "Tipo obrigatorio")
        TransactionType tipo
) { }
