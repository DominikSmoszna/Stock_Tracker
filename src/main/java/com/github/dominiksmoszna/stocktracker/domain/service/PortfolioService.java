package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.common.exception.PortfolioPositionNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ManagePortfolioUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadPortfolioPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ManagePortfolioPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PortfolioService implements ManagePortfolioUseCase {

    private final LoadPortfolioPort loadPortfolioPort;
    private final ManagePortfolioPort managePortfolioPort;

    @Override
    public PortfolioPosition addPosition(UUID userId, String ticker, BigDecimal quantity, BigDecimal purchasePrice, LocalDate purchaseDate) {
        UUID id = UUID.randomUUID();
        PortfolioPosition portfolioPosition = new PortfolioPosition(id, userId, ticker, quantity, purchasePrice, purchaseDate);
        managePortfolioPort.savePosition(portfolioPosition);
        return portfolioPosition;
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
