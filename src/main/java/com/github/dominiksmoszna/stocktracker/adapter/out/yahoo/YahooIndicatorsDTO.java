package com.github.dominiksmoszna.stocktracker.adapter.out.yahoo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record YahooIndicatorsDTO(
        @JsonProperty("quote")
        List<YahooQuoteDTO> quote
) {
}
