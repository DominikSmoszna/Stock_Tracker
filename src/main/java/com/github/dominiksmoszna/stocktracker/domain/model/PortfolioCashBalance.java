package com.github.dominiksmoszna.stocktracker.domain.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public record PortfolioCashBalance(
        UUID userId,
        Currency currency,
        BigDecimal cashBalance,
        BigDecimal netDeposits
) {
}
