package com.github.dominiksmoszna.stocktracker.application.dto;

import java.math.BigDecimal;

public record MarketQuoteDto(
        BigDecimal currentPrice,
        BigDecimal change,
        BigDecimal percentChange,
        BigDecimal high,
        BigDecimal low,
        BigDecimal open,
        BigDecimal previousClose,
        long timestamp,
        String symbol
) {
}
