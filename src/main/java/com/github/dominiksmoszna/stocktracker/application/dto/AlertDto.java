package com.github.dominiksmoszna.stocktracker.application.dto;

import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;

import java.math.BigDecimal;
import java.util.UUID;

public record AlertDto(
        UUID id,
        UUID userId,
        String ticker,
        BigDecimal threshold,
        AlertType type
) {
}
