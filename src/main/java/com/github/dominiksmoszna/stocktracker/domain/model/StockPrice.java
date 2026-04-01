package com.github.dominiksmoszna.stocktracker.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
public class StockPrice {
    String ticker;
    BigDecimal price;
    Instant timestamp;

    public String getTicker() {
        return ticker;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
