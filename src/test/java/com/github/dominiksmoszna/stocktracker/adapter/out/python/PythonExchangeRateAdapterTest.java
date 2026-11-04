package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.github.dominiksmoszna.stocktracker.domain.model.ExchangeRatePoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Unit tests — HTTP layer mocked with MockRestServiceServer, no running Python service needed.
 */
class PythonExchangeRateAdapterTest {

    private static final String BASE_URL = "http://python.test";

    private MockRestServiceServer server;
    private PythonExchangeRateAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new PythonExchangeRateAdapter(builder.build());
    }

    @Test
    void shouldCallFxEndpointWithContractParamNamesAndMapResponse() {
        server.expect(requestTo(BASE_URL + "/api/fx/USDPLN?start_date=2026-01-02&end_date=2026-01-03"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [
                          {"date": "2026-01-02", "rate": 4.0152},
                          {"date": "2026-01-03", "rate": 4.0198}
                        ]
                        """, MediaType.APPLICATION_JSON));

        List<ExchangeRatePoint> rates = adapter.getRates(
                Currency.getInstance("USD"),
                Currency.getInstance("PLN"),
                LocalDate.of(2026, 1, 2),
                LocalDate.of(2026, 1, 3)
        );

        server.verify();
        assertThat(rates).containsExactly(
                new ExchangeRatePoint(LocalDate.of(2026, 1, 2), new BigDecimal("4.0152")),
                new ExchangeRatePoint(LocalDate.of(2026, 1, 3), new BigDecimal("4.0198"))
        );
    }

    @Test
    void shouldReturnEmptyListWhenPythonReturnsNoRates() {
        server.expect(requestTo(BASE_URL + "/api/fx/USDPLN?start_date=2026-01-02&end_date=2026-01-03"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        List<ExchangeRatePoint> rates = adapter.getRates(
                Currency.getInstance("USD"),
                Currency.getInstance("PLN"),
                LocalDate.of(2026, 1, 2),
                LocalDate.of(2026, 1, 3)
        );

        server.verify();
        assertThat(rates).isEmpty();
    }

    @Test
    void shouldReturnIdentitySeriesWithoutCallingPython() {
        List<ExchangeRatePoint> rates = adapter.getRates(
                Currency.getInstance("PLN"),
                Currency.getInstance("PLN"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 3)
        );

        server.verify(); // no expectations registered -> fails if any HTTP call was made
        assertThat(rates)
                .extracting(ExchangeRatePoint::date)
                .containsExactly(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 2),
                        LocalDate.of(2026, 1, 3)
                );
        assertThat(rates)
                .extracting(ExchangeRatePoint::rate)
                .containsOnly(BigDecimal.ONE);
    }
}
