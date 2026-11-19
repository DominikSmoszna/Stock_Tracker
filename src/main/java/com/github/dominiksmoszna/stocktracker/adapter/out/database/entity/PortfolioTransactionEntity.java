package com.github.dominiksmoszna.stocktracker.adapter.out.database.entity;

import com.github.dominiksmoszna.stocktracker.domain.model.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "portfolio_transaction",
    indexes = {
        @Index(name = "idx_portfolio_transaction_user_date", columnList = "user_id, transaction_date"),
        @Index(name = "idx_portfolio_transaction_position_id", columnList = "position_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PortfolioTransactionEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;
    @Column(name = "transaction_date", nullable = false)
    private LocalDate date;
    @Column(name = "position_id")
    private UUID positionId;
    @Column(name = "ticker")
    private String ticker;
    @Column(name = "quantity", precision = 19, scale = 4)
    private BigDecimal quantity;
    @Column(name = "price_per_unit", precision = 19, scale = 4)
    private BigDecimal pricePerUnit;
    @Column(name = "currency", length = 3, nullable = false)
    private String  currency;
    @Column(name = "fx_rate_to_pln", nullable = false, precision = 19, scale = 4)
    private BigDecimal fxRateToPln;
    @Column(name = "amount_pln", nullable = false, precision = 19, scale = 4)
    private BigDecimal amountPln;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PortfolioTransactionEntity that = (PortfolioTransactionEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
