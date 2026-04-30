package com.github.dominiksmoszna.stocktracker.adapter.out.fmp;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FmpHistoricalPriceDto(
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
