package com.github.dominiksmoszna.stocktracker.adapter.out.database.entity;

import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "alert")
@NoArgsConstructor
@Getter
public class AlertEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "userId", nullable = false)
    private UUID userId;
    @Column(name = "ticker", nullable = false)
    private String ticker;
    @Column(name = "threshold", nullable = false)
    private BigDecimal threshold;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AlertType type;

    public  AlertEntity(UUID id, UUID userId, String ticker, BigDecimal threshold, AlertType type) {
        this.id = id;
        this.userId = userId;
        this.ticker = ticker;
        this.threshold = threshold;
        this.type = type;
    }
}
