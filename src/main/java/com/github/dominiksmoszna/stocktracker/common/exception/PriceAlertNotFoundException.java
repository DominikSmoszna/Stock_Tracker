package com.github.dominiksmoszna.stocktracker.common.exception;

public class PriceAlertNotFoundException extends RuntimeException {
    public PriceAlertNotFoundException(String message) {
        super(message);
    }
}
