package com.github.dominiksmoszna.stocktracker.adapter.out.yahoo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record YahooQuoteDTO(
        @JsonProperty("open")
        List<Double> open,
        @JsonProperty("high")
        List<Double> high,
        @JsonProperty("low")
        List<Double> low,
        @JsonProperty("close")
        List<Double> close,
        @JsonProperty("volume")
        List<Double> volume
) {
}
