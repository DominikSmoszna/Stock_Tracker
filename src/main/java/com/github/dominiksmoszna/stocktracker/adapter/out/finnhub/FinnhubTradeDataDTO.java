package com.github.dominiksmoszna.stocktracker.adapter.out.finnhub;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record FinnhubTradeDataDTO(
        @JsonProperty("s")
        String symbol,
        @JsonProperty("p")
        BigDecimal price,
        @JsonProperty("t")
        long timestamp
) {
}
