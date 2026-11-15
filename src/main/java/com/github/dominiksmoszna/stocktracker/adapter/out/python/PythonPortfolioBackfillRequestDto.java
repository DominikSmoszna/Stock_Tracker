package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PythonPortfolioBackfillRequestDto(
        List<PythonPositionInputDto> positions,
        @JsonProperty("target_currency") String targetCurrency,
        @JsonProperty("end_date") LocalDate endDate
) {
}
