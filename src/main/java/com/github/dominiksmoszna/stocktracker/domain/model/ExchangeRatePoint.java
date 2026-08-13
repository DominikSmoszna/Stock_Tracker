package com.github.dominiksmoszna.stocktracker.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRatePoint(
        LocalDate date,
        BigDecimal rate
) {
}
