package com.github.dominiksmoszna.stocktracker.adapter.out.cache;

import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadAlertPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemoryActiveAlertCacheTest {

    @Mock
    private LoadAlertPort loadAlertPort;

    private static PriceAlert alert(String ticker) {
        return PriceAlert.active(UUID.randomUUID(), UUID.randomUUID(), ticker, BigDecimal.TEN, AlertType.ABOVE);
    }

    @Test
    void shouldWarmUpFromActiveAlertsAndGroupByTicker() {
        PriceAlert aapl = alert("AAPL");
        PriceAlert msft = alert("MSFT");
        when(loadAlertPort.loadActivePriceAlerts()).thenReturn(List.of(aapl, msft));
        InMemoryActiveAlertCache cache = new InMemoryActiveAlertCache(loadAlertPort);

        cache.loadActiveAlerts();

        assertThat(cache.getActiveAlerts("AAPL")).containsExactly(aapl);
        assertThat(cache.getActiveAlerts("MSFT")).containsExactly(msft);
        assertThat(cache.getActiveAlerts("TSLA")).isEmpty();
    }

    @Test
    void shouldIgnoreTriggeredAlerts() {
        InMemoryActiveAlertCache cache = new InMemoryActiveAlertCache(loadAlertPort);

        cache.addActiveAlert(alert("AAPL").markTriggered(Instant.now()));

        assertThat(cache.getActiveAlerts("AAPL")).isEmpty();
    }

    @Test
    void removeShouldReturnTrueOnlyForFirstRemoval() {
        InMemoryActiveAlertCache cache = new InMemoryActiveAlertCache(loadAlertPort);
        PriceAlert aapl = alert("AAPL");
        cache.addActiveAlert(aapl);

        assertThat(cache.removeActiveAlert(aapl)).isTrue();
        assertThat(cache.removeActiveAlert(aapl)).isFalse();
        assertThat(cache.getActiveAlerts("AAPL")).isEmpty();
    }
}
