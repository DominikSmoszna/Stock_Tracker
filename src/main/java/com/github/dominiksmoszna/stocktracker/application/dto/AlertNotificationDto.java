package com.github.dominiksmoszna.stocktracker.application.dto;

import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Payload published on the STOMP topic {@code /topic/alerts} when a price alert fires.
 */
public record AlertNotificationDto(
        UUID alertId,
        UUID userId,
        String ticker,
        AlertType type,
        BigDecimal threshold,
        BigDecimal currentPrice,
        Instant triggeredAt
) {
}
