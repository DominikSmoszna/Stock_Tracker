package com.github.dominiksmoszna.stocktracker.adapter.out.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="stock_price")
@NoArgsConstructor
@Getter
public class StockPriceEntity {
    @Id
    @Column(name = "id")
    UUID id;
    @Column(name = "ticker")
    String ticker;
    @Column(name = "price")
    BigDecimal price;
    @Column(name = "timestamp")
    Instant timestamp;

    public StockPriceEntity(UUID id, String ticker, BigDecimal price, Instant timestamp) {
        this.id = id;
        this.ticker = ticker;
        this.price = price;
        this.timestamp = timestamp;
    }
}
