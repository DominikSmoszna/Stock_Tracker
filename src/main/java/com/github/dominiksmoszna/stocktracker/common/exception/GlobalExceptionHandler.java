package com.github.dominiksmoszna.stocktracker.common.exception;

import com.github.dominiksmoszna.stocktracker.application.dto.ErrorResponse;
import com.github.dominiksmoszna.stocktracker.application.dto.PortfolioValuationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WatchlistNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWatchlistNotFoundException(WatchlistNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(PriceAlertNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePriceAlertNotFoundException(PriceAlertNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(PortfolioPositionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePortfolioPositionNotFoundException(PortfolioPositionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(SymbolNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSymbolNotFoundException(SymbolNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(PortfolioValuationException.class)
    public ResponseEntity<PortfolioValuationErrorResponse> handlePortfolioValuationException(PortfolioValuationException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new PortfolioValuationErrorResponse(
                e.getMessage(),
                e.getFailedPriceSymbols(),
                e.getFailedFxSymbols()
        ));
    }
}
