package com.github.dominiksmoszna.stocktracker.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PortfolioPositionDto(
        UUID id,
        UUID userId,
        String ticker,
        BigDecimal quantity,
        BigDecimal purchasePrice,
        LocalDate purchaseDate
) {
}
