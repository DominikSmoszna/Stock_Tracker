package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;

import java.util.List;
import java.util.UUID;

public interface LoadPortfolioPort {
    List<PortfolioPosition> loadPositions(UUID userId);
    PortfolioPosition loadPosition(UUID positionId);
}
