package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record PythonPositionHistoryDto(
        @JsonProperty("position_id") UUID positionId,
        String symbol,
        List<PythonPositionValuePointDto> values
) {
}
