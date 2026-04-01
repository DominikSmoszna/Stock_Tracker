package com.github.dominiksmoszna.stocktracker.common.exception;

public class WatchlistNotFoundException extends RuntimeException {
    public WatchlistNotFoundException(String message) {
        super(message);
    }
}
