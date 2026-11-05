package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.common.exception.PriceAlertNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ActiveAlertCachePort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadAlertPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ManageAlertPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private LoadAlertPort loadAlertPort;

    @Mock
    private ManageAlertPort manageAlertPort;

    @Mock
    private ActiveAlertCachePort activeAlertCachePort;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(loadAlertPort, manageAlertPort, activeAlertCachePort);
    }

    @Test
    void shouldPersistAndCacheActiveAlertOnAdd() {
        UUID userId = UUID.randomUUID();

        PriceAlert created = alertService.addAlert(userId, "AAPL", new BigDecimal("150.00"), AlertType.ABOVE);

        assertThat(created.userId()).isEqualTo(userId);
        assertThat(created.ticker()).isEqualTo("AAPL");
        assertThat(created.threshold()).isEqualByComparingTo("150.00");
        assertThat(created.type()).isEqualTo(AlertType.ABOVE);
        assertThat(created.triggered()).isFalse();
        assertThat(created.triggeredAt()).isNull();

        verify(manageAlertPort).addAlert(created);
        verify(activeAlertCachePort).addActiveAlert(created);
    }

    @Test
    void shouldRemoveAlertFromDatabaseAndCache() {
        UUID alertId = UUID.randomUUID();
        PriceAlert existing = PriceAlert.active(alertId, UUID.randomUUID(), "AAPL", new BigDecimal("150.00"), AlertType.ABOVE);
        when(loadAlertPort.loadPriceAlert(alertId)).thenReturn(existing);

        alertService.removeAlert(alertId);

        verify(manageAlertPort).removeAlert(alertId);
        verify(activeAlertCachePort).removeActiveAlert(existing);
    }

    @Test
    void shouldThrowWhenRemovingNonExistentAlert() {
        UUID alertId = UUID.randomUUID();
        when(loadAlertPort.loadPriceAlert(alertId)).thenReturn(null);

        assertThatThrownBy(() -> alertService.removeAlert(alertId))
                .isInstanceOf(PriceAlertNotFoundException.class);

        verifyNoInteractions(manageAlertPort, activeAlertCachePort);
    }
}
