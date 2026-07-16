package com.github.dominiksmoszna.stocktracker.common.mapper;

import com.github.dominiksmoszna.stocktracker.application.dto.PortfolioPositionDto;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;

public class PortfolioMapper {

    private PortfolioMapper() {}

    public static PortfolioPositionDto toDto(PortfolioPosition portfolioPosition) {
        return new PortfolioPositionDto(
                portfolioPosition.id(),
                portfolioPosition.userId(),
                portfolioPosition.ticker(),
                portfolioPosition.quantity(),
                portfolioPosition.purchasePrice(),
                portfolioPosition.purchaseDate()
        );
    }
}
