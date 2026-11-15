package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.common.exception.SymbolNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.InstrumentInfo;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioValuationResult;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioValuePoint;
import com.github.dominiksmoszna.stocktracker.domain.model.PositionValuationHistory;
import com.github.dominiksmoszna.stocktracker.domain.model.PositionValuePoint;
import com.github.dominiksmoszna.stocktracker.domain.port.out.InstrumentInfoPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadPortfolioPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ManagePortfolioPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.PortfolioValuationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final BigDecimal QUANTITY = new BigDecimal("10");
    private static final BigDecimal PRICE = new BigDecimal("0.72");
    private static final LocalDate DATE = LocalDate.of(2026, 1, 2);

    @Mock
    private LoadPortfolioPort loadPortfolioPort;

    @Mock
    private ManagePortfolioPort managePortfolioPort;

    @Mock
    private InstrumentInfoPort instrumentInfoPort;

    @Mock
    private PortfolioValuationPort portfolioValuationPort;

    private PortfolioService portfolioService;

    @BeforeEach
    void setUp() {
        portfolioService = new PortfolioService(
                loadPortfolioPort,
                managePortfolioPort,
                instrumentInfoPort,
                portfolioValuationPort
        );
    }

    @Test
    void shouldResolveCurrencyFromInstrumentWhenNotProvided() {
        when(instrumentInfoPort.loadInstrument("VOD.L"))
                .thenReturn(Optional.of(new InstrumentInfo("VOD.L", "Vodafone", Currency.getInstance("GBP"))));

        PortfolioPosition position = portfolioService.addPosition(USER_ID, " vod.l ", QUANTITY, PRICE, DATE, null);

        assertThat(position.currency()).isEqualTo(Currency.getInstance("GBP"));
        assertThat(position.ticker()).isEqualTo("VOD.L");
        verify(managePortfolioPort).savePosition(position);
    }

    @Test
    void shouldUseExplicitCurrencyWithoutCallingInstrumentService() {
        PortfolioPosition position = portfolioService.addPosition(
                USER_ID, "AAPL", QUANTITY, PRICE, DATE, Currency.getInstance("USD"));

        assertThat(position.currency()).isEqualTo(Currency.getInstance("USD"));
        verifyNoInteractions(instrumentInfoPort);
        verify(managePortfolioPort).savePosition(position);
    }

    @Test
    void shouldThrowAndNotSaveWhenInstrumentUnknown() {
        when(instrumentInfoPort.loadInstrument("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.addPosition(USER_ID, "NOPE", QUANTITY, PRICE, DATE, null))
                .isInstanceOf(SymbolNotFoundException.class);
        verify(managePortfolioPort, never()).savePosition(any());
    }

    @Test
    void shouldDelegatePortfolioValuationWithExplicitCurrencyAndEndDate() {
        UUID positionId = UUID.randomUUID();
        List<PortfolioPosition> positions = List.of(
                new PortfolioPosition(positionId, USER_ID, "AAPL", QUANTITY, new BigDecimal("180.00"), DATE, Currency.getInstance("USD"))
        );
        LocalDate endDate = LocalDate.of(2026, 1, 10);
        Currency usd = Currency.getInstance("USD");
        PortfolioValuationResult expectedResult = new PortfolioValuationResult(
                List.of(new PortfolioValuePoint(DATE, new BigDecimal("1800.00"))),
                List.of(new PositionValuationHistory(
                        positionId,
                        "AAPL",
                        List.of(new PositionValuePoint(DATE, new BigDecimal("1800.00")))
                ))
        );

        when(loadPortfolioPort.loadPositions(USER_ID)).thenReturn(positions);
        when(portfolioValuationPort.getHistoricalValuation(positions, usd, endDate)).thenReturn(expectedResult);

        PortfolioValuationResult result = portfolioService.getPortfolioValuation(USER_ID, usd, endDate);

        assertThat(result).isEqualTo(expectedResult);
        verify(loadPortfolioPort).loadPositions(USER_ID);
        verify(portfolioValuationPort).getHistoricalValuation(positions, usd, endDate);
    }

    @Test
    void shouldDefaultTargetCurrencyToPlnWhenNull() {
        List<PortfolioPosition> positions = List.of();
        Currency pln = Currency.getInstance("PLN");
        PortfolioValuationResult expectedResult = new PortfolioValuationResult(List.of(), List.of());

        when(loadPortfolioPort.loadPositions(USER_ID)).thenReturn(positions);
        when(portfolioValuationPort.getHistoricalValuation(positions, pln, null)).thenReturn(expectedResult);

        PortfolioValuationResult result = portfolioService.getPortfolioValuation(USER_ID, null, null);

        assertThat(result).isEqualTo(expectedResult);
        verify(portfolioValuationPort).getHistoricalValuation(positions, pln, null);
    }
}
