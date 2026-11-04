package com.github.dominiksmoszna.stocktracker.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PositionValuePoint(
        LocalDate date,
        BigDecimal value
) {
}
