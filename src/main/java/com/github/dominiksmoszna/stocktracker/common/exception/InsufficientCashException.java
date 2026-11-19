package com.github.dominiksmoszna.stocktracker.common.exception;

public class InsufficientCashException extends RuntimeException {
    public InsufficientCashException(String message) {
        super(message);
    }
}
