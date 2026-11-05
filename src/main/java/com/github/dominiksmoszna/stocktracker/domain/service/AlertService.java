package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.common.exception.PriceAlertNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ManageAlertUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ActiveAlertCachePort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadAlertPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ManageAlertPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AlertService implements ManageAlertUseCase {

    private final LoadAlertPort loadAlertPort;
    private final ManageAlertPort manageAlertPort;
    private final ActiveAlertCachePort activeAlertCachePort;


    @Override
    public PriceAlert addAlert(UUID userId, String ticker, BigDecimal threshold, AlertType type) {
        UUID id = UUID.randomUUID();
        PriceAlert priceAlert = PriceAlert.active(id, userId, ticker, threshold, type);
        manageAlertPort.addAlert(priceAlert);
        activeAlertCachePort.addActiveAlert(priceAlert);
        return priceAlert;
    }

    @Override
    public void removeAlert(UUID alertId) {
        PriceAlert priceAlert = Optional.ofNullable(loadAlertPort.loadPriceAlert(alertId))
                .orElseThrow(() -> new PriceAlertNotFoundException("Alert not found"));
        manageAlertPort.removeAlert(alertId);
        activeAlertCachePort.removeActiveAlert(priceAlert);
    }

    @Override
    public List<PriceAlert> getPriceAlerts(UUID userId) {
        return loadAlertPort.loadPriceAlerts(userId);
    }

    @Override
    public List<PriceAlert> getPriceAlertsByTicker(UUID userId, String ticker) {
        return loadAlertPort.loadPriceAlertsByTicker(userId, ticker);
    }
}
