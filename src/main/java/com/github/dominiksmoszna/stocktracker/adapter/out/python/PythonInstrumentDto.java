package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PythonInstrumentDto(
        String symbol,
        String name,
        String currency,
        @JsonProperty("quote_currency") String quoteCurrency,
        @JsonProperty("price_divisor") Integer priceDivisor
) {
}
