package com.github.dominiksmoszna.stocktracker.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AddTickerRequest(
        @NotBlank
        String ticker
) {
}
