package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.github.dominiksmoszna.stocktracker.domain.model.ExchangeRatePoint;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ExchangeRatePort;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Calls the real Python analysis service (and Yahoo Finance behind it).
 * Requires the service running on http://localhost:8000 — excluded from the default {@code mvn test} run.
 */
@Tag("integration")
class PythonExchangeRateAdapterIntegrationTest {

    @Test
    void shouldFetchUsdPlnRates() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8000")
                .build();

        ExchangeRatePort adapter = new PythonExchangeRateAdapter(restClient);

        List<ExchangeRatePoint> rates = adapter.getRates(
                Currency.getInstance("USD"),
                Currency.getInstance("PLN"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 10)
        );

        assertThat(rates).isNotEmpty();

        ExchangeRatePoint first = rates.getFirst();

        assertThat(first.date()).isNotNull();
        assertThat(first.rate()).isNotNull();
        assertThat(first.rate()).isPositive();
    }
}
