package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.github.dominiksmoszna.stocktracker.common.exception.PortfolioValuationException;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioValuationResult;
import com.github.dominiksmoszna.stocktracker.domain.port.out.PortfolioValuationPort;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Calls the real Python analysis service (and Yahoo Finance behind it).
 * Requires the service running on http://localhost:8000 — excluded from the default {@code mvn test} run.
 */
@Tag("integration")
class PythonPortfolioValuationAdapterIntegrationTest {

    @Test
    void shouldFetchHistoricalPortfolioValuation() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8000")
                .build();

        PortfolioValuationPort adapter = new PythonPortfolioValuationAdapter(restClient);

        UUID positionId = UUID.randomUUID();
        PortfolioPosition position = new PortfolioPosition(
                positionId,
                UUID.randomUUID(),
                "AAPL",
                new BigDecimal("2"),
                new BigDecimal("180.00"),
                LocalDate.of(2026, 1, 2),
                Currency.getInstance("USD")
        );

        PortfolioValuationResult result = adapter.getHistoricalValuation(
                List.of(position),
                Currency.getInstance("PLN"),
                LocalDate.of(2026, 1, 5)
        );

        assertThat(result.total()).isNotEmpty();
        assertThat(result.total().getFirst().totalValue()).isPositive();
        assertThat(result.positions()).hasSize(1);
        assertThat(result.positions().getFirst().positionId()).isEqualTo(positionId);
        assertThat(result.positions().getFirst().symbol()).isEqualTo("AAPL");
        assertThat(result.positions().getFirst().values()).hasSameSizeAs(result.total());
    }

    @Test
    void shouldThrowPortfolioValuationExceptionForInvalidSymbol() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8000")
                .build();

        PortfolioValuationPort adapter = new PythonPortfolioValuationAdapter(restClient);

        PortfolioPosition invalidPosition = new PortfolioPosition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "NOTAREALTICKERXYZ123",
                BigDecimal.ONE,
                BigDecimal.TEN,
                LocalDate.of(2026, 1, 2),
                Currency.getInstance("USD")
        );

        assertThatThrownBy(() -> adapter.getHistoricalValuation(
                List.of(invalidPosition),
                Currency.getInstance("PLN"),
                LocalDate.of(2026, 1, 5)
        ))
                .isInstanceOf(PortfolioValuationException.class)
                .satisfies(ex -> {
                    PortfolioValuationException valuationEx = (PortfolioValuationException) ex;
                    assertThat(valuationEx.getFailedPriceSymbols()).containsExactly("NOTAREALTICKERXYZ123");
                });
    }
}
