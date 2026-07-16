package com.github.dominiksmoszna.stocktracker.common.exception;

public class PortfolioPositionNotFoundException extends RuntimeException {
    public PortfolioPositionNotFoundException(String message) {
        super(message);
    }
}
