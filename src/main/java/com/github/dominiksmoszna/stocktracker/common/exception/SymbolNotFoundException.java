package com.github.dominiksmoszna.stocktracker.common.exception;

public class SymbolNotFoundException extends RuntimeException {
    public SymbolNotFoundException(String message) {
        super(message);
    }
}
