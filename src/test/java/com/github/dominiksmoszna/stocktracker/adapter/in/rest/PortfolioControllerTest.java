package com.github.dominiksmoszna.stocktracker.adapter.in.rest;

import com.github.dominiksmoszna.stocktracker.common.exception.GlobalExceptionHandler;
import com.github.dominiksmoszna.stocktracker.common.exception.PortfolioValuationException;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioValuationResult;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioValuePoint;
import com.github.dominiksmoszna.stocktracker.domain.model.PositionValuationHistory;
import com.github.dominiksmoszna.stocktracker.domain.model.PositionValuePoint;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ManagePortfolioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioControllerTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID POSITION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private ManagePortfolioUseCase managePortfolioUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PortfolioController(managePortfolioUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnPortfolioValuationWithDefaultCurrencyPln() throws Exception {
        Currency pln = Currency.getInstance("PLN");
        PortfolioValuationResult result = new PortfolioValuationResult(
                List.of(new PortfolioValuePoint(LocalDate.of(2026, 1, 2), new BigDecimal("7200.50"))),
                List.of(new PositionValuationHistory(
                        POSITION_ID,
                        "AAPL",
                        List.of(new PositionValuePoint(LocalDate.of(2026, 1, 2), new BigDecimal("7200.50")))
                ))
        );

        when(managePortfolioUseCase.getPortfolioValuation(USER_ID, pln, null)).thenReturn(result);

        mockMvc.perform(get("/api/portfolio/{userId}/valuation", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetCurrency").value("PLN"))
                .andExpect(jsonPath("$.total[0].date").value("2026-01-02"))
                .andExpect(jsonPath("$.total[0].totalValue").value(7200.50))
                .andExpect(jsonPath("$.positions[0].positionId").value(POSITION_ID.toString()))
                .andExpect(jsonPath("$.positions[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$.positions[0].values[0].date").value("2026-01-02"))
                .andExpect(jsonPath("$.positions[0].values[0].value").value(7200.50));

        verify(managePortfolioUseCase).getPortfolioValuation(USER_ID, pln, null);
    }

    @Test
    void shouldPassExplicitCurrencyAndEndDateToUseCase() throws Exception {
        Currency usd = Currency.getInstance("USD");
        LocalDate endDate = LocalDate.of(2026, 1, 15);
        PortfolioValuationResult result = new PortfolioValuationResult(List.of(), List.of());

        when(managePortfolioUseCase.getPortfolioValuation(USER_ID, usd, endDate)).thenReturn(result);

        mockMvc.perform(get("/api/portfolio/{userId}/valuation", USER_ID)
                        .param("currency", "USD")
                        .param("endDate", "2026-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetCurrency").value("USD"))
                .andExpect(jsonPath("$.total").isEmpty())
                .andExpect(jsonPath("$.positions").isEmpty());

        verify(managePortfolioUseCase).getPortfolioValuation(USER_ID, usd, endDate);
    }

    @Test
    void shouldReturn502BadGatewayWhenValuationThrowsPortfolioValuationException() throws Exception {
        Currency pln = Currency.getInstance("PLN");
        when(managePortfolioUseCase.getPortfolioValuation(USER_ID, pln, null))
                .thenThrow(new PortfolioValuationException(
                        "Failed to fetch historical market data",
                        List.of("INVALID_TICKER"),
                        List.of("USDPLN=X")
                ));

        mockMvc.perform(get("/api/portfolio/{userId}/valuation", USER_ID))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Failed to fetch historical market data"))
                .andExpect(jsonPath("$.failedPriceSymbols[0]").value("INVALID_TICKER"))
                .andExpect(jsonPath("$.failedFxSymbols[0]").value("USDPLN=X"));
    }
}
