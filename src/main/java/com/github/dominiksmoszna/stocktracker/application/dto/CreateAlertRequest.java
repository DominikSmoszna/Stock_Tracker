package com.github.dominiksmoszna.stocktracker.application.dto;

import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAlertRequest(
        @NotNull
        UUID userId,
        @NotBlank
        String ticker,
        @NotNull
        BigDecimal threshold,
        @NotNull
        AlertType type
) {
}
