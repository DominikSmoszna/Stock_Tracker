package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.github.dominiksmoszna.stocktracker.common.exception.PortfolioValuationException;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioValuationResult;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioValuePoint;
import com.github.dominiksmoszna.stocktracker.domain.model.PositionValuationHistory;
import com.github.dominiksmoszna.stocktracker.domain.model.PositionValuePoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Unit tests — HTTP layer mocked with MockRestServiceServer, no running Python service needed.
 */
class PythonPortfolioValuationAdapterTest {

    private static final String BASE_URL = "http://python.test";

    private MockRestServiceServer server;
    private PythonPortfolioValuationAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new PythonPortfolioValuationAdapter(builder.build());
    }

    @Test
    void shouldPostBackfillRequestWithSnakeCaseFieldsAndMapResponse() {
        UUID positionId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        PortfolioPosition position = new PortfolioPosition(
                positionId,
                userId,
                "AAPL",
                new BigDecimal("2"),
                new BigDecimal("180.00"),
                LocalDate.of(2026, 1, 2),
                Currency.getInstance("USD")
        );

        server.expect(requestTo(BASE_URL + "/api/portfolio/backfill"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.target_currency").value("PLN"))
                .andExpect(jsonPath("$.end_date").value("2026-01-03"))
                .andExpect(jsonPath("$.positions[0].position_id").value(positionId.toString()))
                .andExpect(jsonPath("$.positions[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$.positions[0].buy_date").value("2026-01-02"))
                .andExpect(jsonPath("$.positions[0].quantity").value(2))
                .andExpect(jsonPath("$.positions[0].currency").value("USD"))
                .andRespond(withSuccess("""
                        {
                          "total": [
                            {"date": "2026-01-02", "total_value": 1450.25},
                            {"date": "2026-01-03", "total_value": 1462.80}
                          ],
                          "positions": [
                            {
                              "position_id": "11111111-2222-3333-4444-555555555555",
                              "symbol": "AAPL",
                              "values": [
                                {"date": "2026-01-02", "value": 1450.25},
                                {"date": "2026-01-03", "value": 1462.80}
                              ]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        PortfolioValuationResult result = adapter.getHistoricalValuation(
                List.of(position),
                Currency.getInstance("PLN"),
                LocalDate.of(2026, 1, 3)
        );

        server.verify();
        assertThat(result.total()).containsExactly(
                new PortfolioValuePoint(LocalDate.of(2026, 1, 2), new BigDecimal("1450.25")),
                new PortfolioValuePoint(LocalDate.of(2026, 1, 3), new BigDecimal("1462.80"))
        );
        assertThat(result.positions()).containsExactly(
                new PositionValuationHistory(
                        positionId,
                        "AAPL",
                        List.of(
                                new PositionValuePoint(LocalDate.of(2026, 1, 2), new BigDecimal("1450.25")),
                                new PositionValuePoint(LocalDate.of(2026, 1, 3), new BigDecimal("1462.80"))
                        )
                )
        );
    }

    @Test
    void shouldReturnEmptyResultWithoutCallingPythonWhenPositionsEmpty() {
        PortfolioValuationResult result = adapter.getHistoricalValuation(
                List.of(),
                Currency.getInstance("PLN"),
                LocalDate.of(2026, 1, 3)
        );

        server.verify();
        assertThat(result.total()).isEmpty();
        assertThat(result.positions()).isEmpty();
    }

    @Test
    void shouldThrowPortfolioValuationExceptionWithParsedSymbolsOn502() {
        PortfolioPosition invalidPosition = new PortfolioPosition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "INVALID_TICKER",
                BigDecimal.ONE,
                BigDecimal.TEN,
                LocalDate.of(2026, 1, 2),
                Currency.getInstance("USD")
        );

        server.expect(requestTo(BASE_URL + "/api/portfolio/backfill"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "detail": {
                                    "message": "Failed to fetch historical market data",
                                    "price_symbols": ["INVALID_TICKER"],
                                    "fx_symbols": ["USD"]
                                  }
                                }
                                """));

        assertThatThrownBy(() -> adapter.getHistoricalValuation(
                List.of(invalidPosition),
                Currency.getInstance("PLN"),
                LocalDate.of(2026, 1, 3)
        ))
                .isInstanceOf(PortfolioValuationException.class)
                .satisfies(ex -> {
                    PortfolioValuationException valuationEx = (PortfolioValuationException) ex;
                    assertThat(valuationEx.getMessage()).isEqualTo("Failed to fetch historical market data");
                    assertThat(valuationEx.getFailedPriceSymbols()).containsExactly("INVALID_TICKER");
                    assertThat(valuationEx.getFailedFxSymbols()).containsExactly("USD");
                });

        server.verify();
    }
}
