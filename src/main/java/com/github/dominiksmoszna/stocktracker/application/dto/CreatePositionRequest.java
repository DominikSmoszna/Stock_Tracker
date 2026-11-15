package com.github.dominiksmoszna.stocktracker.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

public record CreatePositionRequest(
        @NotNull
        UUID userId,
        @NotBlank
        String ticker,
        @NotNull
        BigDecimal quantity,
        @NotNull
        BigDecimal purchasePrice,
        @NotNull
        LocalDate purchaseDate,
        // Optional: resolved from the instrument's trading currency (GET /api/instrument/{symbol}) when omitted
        Currency currency
) {
}
