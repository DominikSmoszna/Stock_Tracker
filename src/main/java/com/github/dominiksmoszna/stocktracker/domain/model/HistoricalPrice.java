package com.github.dominiksmoszna.stocktracker.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoricalPrice(
        String symbol,
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        long volume,
        BigDecimal change,
        BigDecimal changePercent,
        BigDecimal vwap
) {
}
