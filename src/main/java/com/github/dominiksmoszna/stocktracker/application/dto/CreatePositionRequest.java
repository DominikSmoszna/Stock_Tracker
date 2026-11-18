package com.github.dominiksmoszna.stocktracker.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

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
        @Positive
        BigDecimal quantity,
        @NotNull
        @Positive
        BigDecimal purchasePrice,
        @NotNull
        @PastOrPresent
        LocalDate purchaseDate,
        Currency currency
) {
}
