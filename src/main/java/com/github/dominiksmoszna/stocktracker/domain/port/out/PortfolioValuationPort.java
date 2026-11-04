package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.common.exception.PortfolioValuationException;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioValuationResult;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

public interface PortfolioValuationPort {

    PortfolioValuationResult getHistoricalValuation(
            List<PortfolioPosition> positions,
            Currency targetCurrency,
            LocalDate endDate
    ) throws PortfolioValuationException;
}
