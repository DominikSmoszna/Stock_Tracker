package com.github.dominiksmoszna.stocktracker.application.dto;

import java.util.List;

public record PortfolioValuationErrorResponse(
        String message,
        List<String> failedPriceSymbols,
        List<String> failedFxSymbols
) {
}
