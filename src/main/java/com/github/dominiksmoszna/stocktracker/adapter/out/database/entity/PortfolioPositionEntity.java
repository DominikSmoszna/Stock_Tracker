package com.github.dominiksmoszna.stocktracker.adapter.out.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "position")
@NoArgsConstructor
@Getter
public class PortfolioPositionEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "userId", nullable = false)
    private UUID userId;
    @Column(name = "ticker", nullable = false)
    private String ticker;
    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;
    @Column(name = "purchasePrice", nullable = false)
    private BigDecimal purchasePrice;
    @Column(name = "purchaseDate", nullable = false)
    private LocalDate purchaseDate;

    public PortfolioPositionEntity(UUID id, UUID userId, String ticker, BigDecimal quantity, BigDecimal purchasePrice, LocalDate purchaseDate){
        this.id = id;
        this.userId = userId;
        this.ticker = ticker;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
    }

}
