package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.adapter.out.cache.InMemoryActiveAlertCache;
import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.model.StockPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketDataServiceTest {

    private static final String TICKER = "AAPL";
    private static final Instant TS = Instant.parse("2026-11-05T15:00:00Z");

    @Mock private CurrentPricePort currentPricePort;
    @Mock private SaveCurrentPricePort saveCurrentPricePort;
    @Mock private PriceHistoryPort priceHistoryPort;
    @Mock private PriceUpdatePort priceUpdatePort;
    @Mock private SendNotificationPort sendNotificationPort;
    @Mock private ManageAlertPort manageAlertPort;
    @Mock private LoadAlertPort loadAlertPort;

    private InMemoryActiveAlertCache activeAlertCache;
    private MarketDataService service;

    @BeforeEach
    void setUp() {
        activeAlertCache = new InMemoryActiveAlertCache(loadAlertPort);
        service = new MarketDataService(currentPricePort, saveCurrentPricePort, priceHistoryPort,
                priceUpdatePort, sendNotificationPort, activeAlertCache, manageAlertPort);
    }

    private static PriceAlert alert(AlertType type, String threshold) {
        return PriceAlert.active(UUID.randomUUID(), UUID.randomUUID(), TICKER, new BigDecimal(threshold), type);
    }

    private static StockPrice tick(String price) {
        return new StockPrice(TICKER, new BigDecimal(price), TS);
    }

    @Test
    void shouldTriggerAboveAlertOnlyOnceAcrossConsecutiveTicks() {
        PriceAlert alert = alert(AlertType.ABOVE, "100");
        activeAlertCache.addActiveAlert(alert);
        when(manageAlertPort.markAlertTriggered(alert.id(), TS)).thenReturn(true);

        service.processPrice(tick("101"));
        service.processPrice(tick("102"));

        ArgumentCaptor<PriceAlert> captor = ArgumentCaptor.forClass(PriceAlert.class);
        verify(sendNotificationPort, times(1)).sendNotification(captor.capture(), eq(new BigDecimal("101")));
        assertThat(captor.getValue().triggered()).isTrue();
        assertThat(captor.getValue().triggeredAt()).isEqualTo(TS);
        verify(manageAlertPort, times(1)).markAlertTriggered(alert.id(), TS);
        assertThat(activeAlertCache.getActiveAlerts(TICKER)).isEmpty();
        verify(priceUpdatePort, times(2)).updateCurrentPrice(any());
    }

    @Test
    void shouldTriggerBelowAlertWhenPriceDropsUnderThreshold() {
        PriceAlert alert = alert(AlertType.BELOW, "50");
        activeAlertCache.addActiveAlert(alert);
        when(manageAlertPort.markAlertTriggered(alert.id(), TS)).thenReturn(true);

        service.processPrice(tick("49.99"));

        verify(sendNotificationPort).sendNotification(any(), eq(new BigDecimal("49.99")));
    }

    @Test
    void shouldNotTriggerWhenConditionNotMet() {
        activeAlertCache.addActiveAlert(alert(AlertType.ABOVE, "100"));
        activeAlertCache.addActiveAlert(alert(AlertType.BELOW, "90"));

        service.processPrice(tick("100"));

        verifyNoInteractions(manageAlertPort, sendNotificationPort);
        assertThat(activeAlertCache.getActiveAlerts(TICKER)).hasSize(2);
    }

    @Test
    void shouldRestoreAlertInCacheWhenPersistenceFails() {
        PriceAlert alert = alert(AlertType.ABOVE, "100");
        activeAlertCache.addActiveAlert(alert);
        when(manageAlertPort.markAlertTriggered(alert.id(), TS)).thenThrow(new RuntimeException("db down"));

        service.processPrice(tick("101"));

        verifyNoInteractions(sendNotificationPort);
        assertThat(activeAlertCache.getActiveAlerts(TICKER)).containsExactly(alert);
        verify(priceUpdatePort).updateCurrentPrice(any());
    }

    @Test
    void shouldNotNotifyWhenAlertWasDeletedInTheMeantime() {
        PriceAlert alert = alert(AlertType.ABOVE, "100");
        activeAlertCache.addActiveAlert(alert);
        when(manageAlertPort.markAlertTriggered(alert.id(), TS)).thenReturn(false);

        service.processPrice(tick("101"));

        verifyNoInteractions(sendNotificationPort);
        assertThat(activeAlertCache.getActiveAlerts(TICKER)).isEmpty();
    }
}
