package com.github.dominiksmoszna.stocktracker.adapter.out.yahoo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record YahooResponseDTO(
        @JsonProperty("chart")
        YahooChartDTO chart
) {
}
