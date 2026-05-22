package com.github.dominiksmoszna.stocktracker.adapter.out.yahoo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record YahooResultDTO(
        @JsonProperty("meta")
        YahooMetaDTO meta,
        @JsonProperty("timestamp")
        List<Long> timestamp,
        @JsonProperty("indicators")
        YahooIndicatorsDTO indicators
) {
}
