package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.github.dominiksmoszna.stocktracker.common.exception.InstrumentLookupException;
import com.github.dominiksmoszna.stocktracker.domain.model.InstrumentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Unit tests — HTTP layer mocked with MockRestServiceServer, no running Python service needed.
 */
class PythonInstrumentAdapterTest {

    private static final String BASE_URL = "http://python.test";

    private MockRestServiceServer server;
    private PythonInstrumentAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new PythonInstrumentAdapter(builder.build());
    }

    @Test
    void shouldMapNormalizedCurrencyForPenceQuotedInstrument() {
        server.expect(requestTo(BASE_URL + "/api/instrument/VOD.L"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "symbol": "VOD.L",
                          "name": "Vodafone Group Public Limited Company",
                          "currency": "GBP",
                          "quote_currency": "GBp",
                          "price_divisor": 100,
                          "exchange": "LSE",
                          "quote_type": "EQUITY",
                          "sector": "Communication Services",
                          "industry": "Telecom Services",
                          "country": "United Kingdom",
                          "market_cap": 27095721984.0
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<InstrumentInfo> result = adapter.loadInstrument("VOD.L");

        server.verify();
        assertThat(result).contains(new InstrumentInfo(
                "VOD.L",
                "Vodafone Group Public Limited Company",
                Currency.getInstance("GBP")
        ));
    }

    @Test
    void shouldReturnEmptyWhenInstrumentNotFound() {
        server.expect(requestTo(BASE_URL + "/api/instrument/NOPE"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"detail\": \"Instrument not found: NOPE\"}"));

        Optional<InstrumentInfo> result = adapter.loadInstrument("NOPE");

        server.verify();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowLookupExceptionWhenServiceFails() {
        server.expect(requestTo(BASE_URL + "/api/instrument/AAPL"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> adapter.loadInstrument("AAPL"))
                .isInstanceOf(InstrumentLookupException.class);
        server.verify();
    }

    @Test
    void shouldReturnEmptyWhenCurrencyIsMissing() {
        server.expect(requestTo(BASE_URL + "/api/instrument/XYZ"))
                .andRespond(withSuccess("""
                        {"symbol": "XYZ", "name": "Some Index", "currency": null, "price_divisor": 1}
                        """, MediaType.APPLICATION_JSON));

        assertThat(adapter.loadInstrument("XYZ")).isEmpty();
        server.verify();
    }
}
