package com.github.dominiksmoszna.stocktracker.application.dto;

import java.util.Currency;
import java.util.List;

public record PortfolioValuationDto(
        Currency targetCurrency,
        List<PortfolioValuePointDto> total,
        List<PositionValuationHistoryDto> positions
) {
}
