package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.common.exception.PortfolioPositionNotFoundException;
import com.github.dominiksmoszna.stocktracker.common.exception.SymbolNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.InstrumentInfo;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ManagePortfolioUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.InstrumentInfoPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadPortfolioPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ManagePortfolioPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PortfolioService implements ManagePortfolioUseCase {

    private final LoadPortfolioPort loadPortfolioPort;
    private final ManagePortfolioPort managePortfolioPort;
    private final InstrumentInfoPort instrumentInfoPort;

    /**
     * @param currency explicit position currency; when {@code null} it is resolved automatically
     *                 from the instrument's trading currency (normalized, e.g. GBp -> GBP)
     */
    @Override
    public PortfolioPosition addPosition(UUID userId, String ticker, BigDecimal quantity, BigDecimal purchasePrice, LocalDate purchaseDate, Currency currency) {
        String normalizedTicker = ticker.trim().toUpperCase();
        Currency resolvedCurrency = currency != null ? currency : resolveCurrency(normalizedTicker);

        UUID id = UUID.randomUUID();
        PortfolioPosition portfolioPosition = new PortfolioPosition(id, userId, normalizedTicker, quantity, purchasePrice, purchaseDate, resolvedCurrency);
        managePortfolioPort.savePosition(portfolioPosition);
        return portfolioPosition;
    }

    private Currency resolveCurrency(String ticker) {
        return instrumentInfoPort.loadInstrument(ticker)
                .map(InstrumentInfo::currency)
                .orElseThrow(() -> new SymbolNotFoundException("Instrument not found: " + ticker));
    }

    @Override
    public void removePosition(UUID positionId) {
        Optional.ofNullable(loadPortfolioPort.loadPosition(positionId))
                .ifPresentOrElse(portfolioPosition -> {managePortfolioPort.deletePosition(positionId);},
                        () -> {throw new PortfolioPositionNotFoundException("Position not found");});
    }

    @Override
    public List<PortfolioPosition> getPositions(UUID userId) {return loadPortfolioPort.loadPositions(userId);}
}
