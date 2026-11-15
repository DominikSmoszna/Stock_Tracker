package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PythonPositionInputDto(
        @JsonProperty("position_id") UUID positionId,
        String symbol,
        @JsonProperty("buy_date") LocalDate buyDate,
        BigDecimal quantity,
        String currency
) {
}
