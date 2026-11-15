package com.github.dominiksmoszna.stocktracker.common.exception;

/**
 * Thrown when the instrument metadata provider (Python service) is unreachable or returns an unexpected error.
 */
public class InstrumentLookupException extends RuntimeException {
    public InstrumentLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
