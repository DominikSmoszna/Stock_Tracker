package com.github.dominiksmoszna.stocktracker;

import com.github.dominiksmoszna.stocktracker.adapter.out.python.PythonExchangeRateAdapter;
import com.github.dominiksmoszna.stocktracker.domain.model.ExchangeRatePoint;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ExchangeRatePort;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

@SpringBootTest
class StockTrackerApplicationTests {

    @Test
    void contextLoads() {
    }

}

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

    @Test
    void shouldReturnIdentitySeriesWithoutCallingPython() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8000")
                .build();

        ExchangeRatePort adapter = new PythonExchangeRateAdapter(restClient);

        List<ExchangeRatePoint> rates = adapter.getRates(
                Currency.getInstance("PLN"),
                Currency.getInstance("PLN"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 3)
        );

        assertThat(rates).hasSize(3);
        assertThat(rates)
                .extracting(ExchangeRatePoint::rate)
                .containsExactly(
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE
                );
    }
}
