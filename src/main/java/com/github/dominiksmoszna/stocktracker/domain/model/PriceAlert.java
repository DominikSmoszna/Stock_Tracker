package com.github.dominiksmoszna.stocktracker.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PriceAlert(
        UUID id,
        UUID userId,
        String ticker,
        BigDecimal threshold,
        AlertType type,
        boolean triggered,
        Instant triggeredAt
) {

    public static PriceAlert active(UUID id, UUID userId, String ticker, BigDecimal threshold, AlertType type) {
        return new PriceAlert(id, userId, ticker, threshold, type, false, null);
    }

    /**
     * Returns true when the given price satisfies the alert condition and the alert has not fired yet.
     */
    public boolean isTriggeredBy(BigDecimal price) {
        if (triggered || price == null) {
            return false;
        }
        int comparison = price.compareTo(threshold);
        return switch (type) {
            case ABOVE -> comparison > 0;
            case BELOW -> comparison < 0;
        };
    }

    public PriceAlert markTriggered(Instant triggeredAt) {
        return new PriceAlert(id, userId, ticker, threshold, type, true, triggeredAt);
    }
}
