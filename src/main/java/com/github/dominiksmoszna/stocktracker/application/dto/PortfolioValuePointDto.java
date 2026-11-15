package com.github.dominiksmoszna.stocktracker.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PortfolioValuePointDto(
        LocalDate date,
        BigDecimal totalValue
) {
}
