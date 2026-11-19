package com.github.dominiksmoszna.stocktracker.domain.port.in;

import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioCashBalance;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioTransaction;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioValuationResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public interface ManagePortfolioUseCase {
    PortfolioPosition addPosition(UUID userId, String ticker, BigDecimal quantity, BigDecimal purchasePrice, LocalDate purchaseDate, Currency currency);
    void removePosition(UUID positionId);
    List<PortfolioPosition> getPositions(UUID userId);
    PortfolioValuationResult getPortfolioValuation(UUID userId, Currency targetCurrency, LocalDate endDate);
    PortfolioTransaction depositCash(UUID userId, BigDecimal amountPln, LocalDate date);
    PortfolioTransaction withdrawCash(UUID userId, BigDecimal amountPln, LocalDate date);
    PortfolioCashBalance getCashBalance(UUID userId);
    List<PortfolioTransaction> getTransactions(UUID userId);
    PortfolioPosition sellPosition(UUID positionId, BigDecimal sellQuantity, BigDecimal sellPrice, LocalDate sellDate);
}
