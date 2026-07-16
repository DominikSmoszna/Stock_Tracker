package com.github.dominiksmoszna.stocktracker.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PortfolioPosition(
        UUID id,
        UUID userId,
        String ticker,
        BigDecimal quantity,
        BigDecimal purchasePrice,
        LocalDate purchaseDate
) {
}
