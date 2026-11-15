package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PythonErrorResponseDto(
        Detail detail
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Detail(
            String message,
            @JsonProperty("price_symbols") List<String> priceSymbols,
            @JsonProperty("fx_symbols") List<String> fxSymbols
    ) {
    }
}
