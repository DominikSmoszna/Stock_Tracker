package com.github.dominiksmoszna.stocktracker.adapter.out.database.entity;

import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
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
    // DB-level default keeps ddl-auto=update working for already existing rows
    @Column(name = "triggered", nullable = false, columnDefinition = "boolean not null default false")
    private boolean triggered;
    @Column(name = "triggered_at")
    private Instant triggeredAt;

    public  AlertEntity(UUID id, UUID userId, String ticker, BigDecimal threshold, AlertType type,
                        boolean triggered, Instant triggeredAt) {
        this.id = id;
        this.userId = userId;
        this.ticker = ticker;
        this.threshold = threshold;
        this.type = type;
        this.triggered = triggered;
        this.triggeredAt = triggeredAt;
    }
}
