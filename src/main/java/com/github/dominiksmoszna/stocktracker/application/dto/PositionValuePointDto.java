package com.github.dominiksmoszna.stocktracker.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PositionValuePointDto(
        LocalDate date,
        BigDecimal value
) {
}
