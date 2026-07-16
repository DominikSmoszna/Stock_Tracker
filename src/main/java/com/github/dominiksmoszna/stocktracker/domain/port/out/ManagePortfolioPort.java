package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;

import java.util.UUID;

public interface ManagePortfolioPort {
    void savePosition(PortfolioPosition position);
    void deletePosition(UUID positionId);
}
