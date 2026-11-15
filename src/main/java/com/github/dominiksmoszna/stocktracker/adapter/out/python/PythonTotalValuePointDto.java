package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PythonTotalValuePointDto(
        LocalDate date,
        @JsonProperty("total_value") BigDecimal totalValue
) {
}
