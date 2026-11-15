package com.github.dominiksmoszna.stocktracker.application.dto;

import java.util.List;
import java.util.UUID;

public record PositionValuationHistoryDto(
        UUID positionId,
        String symbol,
        List<PositionValuePointDto> values
) {
}
