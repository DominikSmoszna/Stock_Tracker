package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.common.exception.PriceAlertNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ManageAlertUseCase;
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


    @Override
    public PriceAlert addAlert(UUID userId, String ticker, BigDecimal threshold, AlertType type) {
        UUID id = UUID.randomUUID();
        PriceAlert priceAlert = new PriceAlert(id, userId, ticker, threshold, type);
        manageAlertPort.addAlert(priceAlert);
        return priceAlert;
    }

    @Override
    public void removeAlert(UUID alertId) {
        Optional.ofNullable(loadAlertPort.loadPriceAlert(alertId))
                .ifPresentOrElse(priceAlert -> {manageAlertPort.removeAlert(alertId);},
                        () -> {throw new PriceAlertNotFoundException("Alert not found");});
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
