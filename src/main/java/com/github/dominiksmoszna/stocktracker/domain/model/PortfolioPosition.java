package com.github.dominiksmoszna.stocktracker.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

public record PortfolioPosition(
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
    public PortfolioPosition(UUID id, UUID userId, String ticker, BigDecimal quantity, BigDecimal purchasePrice, LocalDate purchaseDate, Currency currency) {
        this(id, userId, ticker, quantity, purchasePrice, purchaseDate, currency, quantity, PositionStatus.OPEN, null);
    }
}
