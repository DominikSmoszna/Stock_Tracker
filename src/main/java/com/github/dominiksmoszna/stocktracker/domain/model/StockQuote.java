package com.github.dominiksmoszna.stocktracker.domain.model;

import java.math.BigDecimal;

public record StockQuote(
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
