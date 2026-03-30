package com.github.dominiksmoszna.stocktracker.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
public class PriceAlert {
    UUID id;
    UUID userId;
    String ticker;
    BigDecimal threshold;
    AlertType type;
}
