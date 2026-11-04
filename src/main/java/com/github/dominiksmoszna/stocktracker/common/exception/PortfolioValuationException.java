package com.github.dominiksmoszna.stocktracker.common.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class PortfolioValuationException extends RuntimeException {

    private final List<String> failedPriceSymbols;
    private final List<String> failedFxSymbols;

    public PortfolioValuationException(
            String message,
            List<String> failedPriceSymbols,
            List<String> failedFxSymbols
    ) {
        super(message);
        this.failedPriceSymbols = List.copyOf(failedPriceSymbols);
        this.failedFxSymbols = List.copyOf(failedFxSymbols);
    }

}
