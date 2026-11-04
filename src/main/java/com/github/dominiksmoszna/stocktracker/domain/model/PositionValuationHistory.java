package com.github.dominiksmoszna.stocktracker.domain.model;

import java.util.List;
import java.util.UUID;

public record PositionValuationHistory(
        UUID positionId,
        String symbol,
        List<PositionValuePoint> values
) {
}
