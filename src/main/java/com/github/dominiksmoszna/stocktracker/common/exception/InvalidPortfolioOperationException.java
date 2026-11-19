package com.github.dominiksmoszna.stocktracker.common.exception;

public class InvalidPortfolioOperationException extends RuntimeException {
    public InvalidPortfolioOperationException(String message) {
        super(message);
    }
}
