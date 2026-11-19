package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioTransaction;

import java.util.List;
import java.util.UUID;

public interface PortfolioTransactionPort {
    void saveTransaction(PortfolioTransaction transaction);
    List<PortfolioTransaction> loadTransactionsByUser(UUID userId);
    List<PortfolioTransaction> loadTransactionsByPositionId(UUID positionId);
    void deleteTransactionsByPositionId(UUID positionId);
}
