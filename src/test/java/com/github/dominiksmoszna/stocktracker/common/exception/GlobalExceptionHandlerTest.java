package com.github.dominiksmoszna.stocktracker.common.exception;

import com.github.dominiksmoszna.stocktracker.application.dto.ErrorResponse;
import com.github.dominiksmoszna.stocktracker.application.dto.PortfolioValuationErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn404ForWatchlistNotFoundException() {
        ResponseEntity<ErrorResponse> response = handler.handleWatchlistNotFoundException(
                new WatchlistNotFoundException("Watchlist not found")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Watchlist not found"));
    }

    @Test
    void shouldReturn404ForPriceAlertNotFoundException() {
        ResponseEntity<ErrorResponse> response = handler.handlePriceAlertNotFoundException(
                new PriceAlertNotFoundException("Alert not found")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Alert not found"));
    }

    @Test
    void shouldReturn404ForPortfolioPositionNotFoundException() {
        ResponseEntity<ErrorResponse> response = handler.handlePortfolioPositionNotFoundException(
                new PortfolioPositionNotFoundException("Position not found")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Position not found"));
    }

    @Test
    void shouldReturn404ForSymbolNotFoundException() {
        ResponseEntity<ErrorResponse> response = handler.handleSymbolNotFoundException(
                new SymbolNotFoundException("No quote data for symbol: INVALID")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("No quote data for symbol: INVALID"));
    }

    @Test
    void shouldReturn502WithFailedSymbolsForPortfolioValuationException() {
        PortfolioValuationException exception = new PortfolioValuationException(
                "Failed to fetch historical market data",
                List.of("BAD_TICKER"),
                List.of("ABCPLN")
        );

        ResponseEntity<PortfolioValuationErrorResponse> response = handler.handlePortfolioValuationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isEqualTo(new PortfolioValuationErrorResponse(
                "Failed to fetch historical market data",
                List.of("BAD_TICKER"),
                List.of("ABCPLN")
        ));
    }
}
