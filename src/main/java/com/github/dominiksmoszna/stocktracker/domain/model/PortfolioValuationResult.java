package com.github.dominiksmoszna.stocktracker.domain.model;

import java.util.List;

public record PortfolioValuationResult(
        List<PortfolioValuePoint> total,
        List<PositionValuationHistory> positions
) {
}
