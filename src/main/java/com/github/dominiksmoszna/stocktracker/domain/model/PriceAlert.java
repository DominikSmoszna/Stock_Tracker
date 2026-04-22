package com.github.dominiksmoszna.stocktracker.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record PriceAlert(UUID id, UUID userId, String ticker, BigDecimal threshold, AlertType type) {
}
