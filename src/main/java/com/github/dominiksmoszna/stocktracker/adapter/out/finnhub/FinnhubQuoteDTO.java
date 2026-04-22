package com.github.dominiksmoszna.stocktracker.adapter.out.finnhub;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record FinnhubQuoteDTO(
        @JsonProperty("c")
        BigDecimal currentPrice,
        @JsonProperty("d")
        BigDecimal change,
        @JsonProperty("dp")
        BigDecimal percentChange,
        @JsonProperty("h")
        BigDecimal high,
        @JsonProperty("l")
        BigDecimal low,
        @JsonProperty("o")
        BigDecimal open,
        @JsonProperty("pc")
        BigDecimal previousClose,
        @JsonProperty("t")
        long timestamp

) {
}
