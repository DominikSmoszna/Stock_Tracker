package com.github.dominiksmoszna.stocktracker.common.mapper;

import com.github.dominiksmoszna.stocktracker.application.dto.*;
import com.github.dominiksmoszna.stocktracker.domain.model.*;
import java.util.Currency;

public class PortfolioMapper {

    private PortfolioMapper() {}

    public static PortfolioPositionDto toDto(PortfolioPosition portfolioPosition) {
        return new PortfolioPositionDto(
                portfolioPosition.id(),
                portfolioPosition.userId(),
                portfolioPosition.ticker(),
                portfolioPosition.quantity(),
                portfolioPosition.purchasePrice(),
                portfolioPosition.purchaseDate(),
                portfolioPosition.currency()
        );
    }

    public static PortfolioValuationDto toValuationDto(
            PortfolioValuationResult result,
            Currency targetCurrency
    ){
        return new PortfolioValuationDto(
                targetCurrency,
                result.total().stream()
                        .map(PortfolioMapper::toPointDto)
                        .toList(),
                result.positions().stream()
                        .map(PortfolioMapper::toPositionHistoryDto)
                        .toList()
        );
    }

    private static PortfolioValuePointDto toPointDto(PortfolioValuePoint point) {
        return new PortfolioValuePointDto(point.date(), point.totalValue());
    }

    private static PositionValuePointDto toPositionPointDto(PositionValuePoint point) {
        return new PositionValuePointDto(point.date(), point.value());
    }

    private static PositionValuationHistoryDto toPositionHistoryDto(PositionValuationHistory history) {
        return new PositionValuationHistoryDto(
                history.positionId(),
                history.symbol(),
                history.values().stream()
                        .map(PortfolioMapper::toPositionPointDto)
                        .toList()
        );
    }

}
