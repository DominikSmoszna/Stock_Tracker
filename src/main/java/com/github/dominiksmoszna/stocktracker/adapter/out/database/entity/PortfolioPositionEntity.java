package com.github.dominiksmoszna.stocktracker.adapter.out.database.entity;

import com.github.dominiksmoszna.stocktracker.domain.model.PositionStatus;
import jakarta.persistence.*;
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
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    @Column(name = "initialQuantity", nullable = false)
    private BigDecimal initialQuantity;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PositionStatus status = PositionStatus.OPEN;
    @Column(name = "closedDate")
    private LocalDate closedDate = null;

    public PortfolioPositionEntity(UUID id, UUID userId, String ticker, BigDecimal quantity, BigDecimal purchasePrice, LocalDate purchaseDate, String currency, BigDecimal initialQuantity, PositionStatus status, LocalDate closedDate){
        this.id = id;
        this.userId = userId;
        this.ticker = ticker;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.currency = currency;
        this.initialQuantity = initialQuantity;
        this.status = status;
        this.closedDate = closedDate;
    }

    public PortfolioPositionEntity(UUID id, UUID userId, String ticker, BigDecimal quantity, BigDecimal purchasePrice, LocalDate purchaseDate, String currency) {
        this(id, userId, ticker, quantity, purchasePrice, purchaseDate, currency, quantity, PositionStatus.OPEN, null);
    }
}
