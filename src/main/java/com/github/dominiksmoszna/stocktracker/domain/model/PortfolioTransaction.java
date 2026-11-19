package com.github.dominiksmoszna.stocktracker.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public record PortfolioTransaction(
        UUID id,
        UUID userId,
        TransactionType type,
        LocalDate date,
        UUID positionId,
        String ticker,
        BigDecimal quantity,
        BigDecimal pricePerUnit,
        Currency currency,
        BigDecimal fxRateToPln,
        BigDecimal amountPln
) {
    public PortfolioTransaction {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
        Objects.requireNonNull(fxRateToPln, "fxRateToPln cannot be null");
        Objects.requireNonNull(amountPln, "amountPln cannot be null");
        if (type == TransactionType.BUY || type == TransactionType.SELL){
            Objects.requireNonNull(positionId, "positionId cannot be null for BUY/SELL transactions");
            if (ticker == null || ticker.isBlank()) {
                throw new IllegalArgumentException("ticker cannot be null or blank for BUY/SELL transactions");
            }
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("quantity must be positive for BUY/SELL transactions");
            }
            if (pricePerUnit == null || pricePerUnit.compareTo(BigDecimal.ZERO) <=0 ) {
                throw new IllegalArgumentException("pricePerUnit must be positive for BUY/SELL transactions");
            }
        }
        if (fxRateToPln.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("fxRateToPln must be positive");
        }
        if (amountPln.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amountPln must be positive");
        }
    }
}
