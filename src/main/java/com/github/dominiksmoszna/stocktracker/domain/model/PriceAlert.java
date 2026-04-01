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

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTicker() {
        return ticker;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public AlertType getType() {
        return type;
    }
}
