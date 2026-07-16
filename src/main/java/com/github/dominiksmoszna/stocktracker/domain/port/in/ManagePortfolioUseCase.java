package com.github.dominiksmoszna.stocktracker.domain.port.in;

import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ManagePortfolioUseCase {
    PortfolioPosition addPosition(UUID userId, String ticker, BigDecimal quantity, BigDecimal purchasePrice, LocalDate purchaseDate);
    void removePosition(UUID positionId);
    List<PortfolioPosition> getPositions(UUID userId);
}
