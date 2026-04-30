package com.github.dominiksmoszna.stocktracker.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record StockPrice(
        String ticker,
        BigDecimal price,
        Instant timestamp) {
}
