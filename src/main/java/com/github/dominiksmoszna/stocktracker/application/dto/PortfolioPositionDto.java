package com.github.dominiksmoszna.stocktracker.application.dto;

import com.github.dominiksmoszna.stocktracker.domain.model.PositionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

public record PortfolioPositionDto(
        UUID id,
        UUID userId,
        String ticker,
        BigDecimal quantity,
        BigDecimal purchasePrice,
        LocalDate purchaseDate,
        Currency currency,
        BigDecimal initialQuantity,
        PositionStatus status,
        LocalDate closedDate
) {
}
