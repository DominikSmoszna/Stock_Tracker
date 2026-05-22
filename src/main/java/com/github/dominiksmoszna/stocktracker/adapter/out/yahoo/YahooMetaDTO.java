package com.github.dominiksmoszna.stocktracker.adapter.out.yahoo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record YahooMetaDTO(
        @JsonProperty("symbol")
        String symbol,
        @JsonProperty("regularMarketPrice")
        BigDecimal regularMarketPrice
) {
}
