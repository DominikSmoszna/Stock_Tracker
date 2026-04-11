package com.github.dominiksmoszna.stocktracker.adapter.out.database;

import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadAlertPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ManageAlertPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class DatabaseAlertAdapter implements LoadAlertPort, ManageAlertPort {
    @Override
    public List<PriceAlert> loadPriceAlerts(UUID userId) {
        log.info("Loading price alerts for user {}", userId);
        return List.of();
    }

    @Override
    public List<PriceAlert> loadPriceAlertsByTicker(UUID userId, String ticker) {
        log.info("Loading price alerts for user {} by ticker {}", userId, ticker);
        return List.of();
    }

    @Override
    public PriceAlert loadPriceAlert(UUID alertId) {
        log.info("Loading price alert {}", alertId);
        return null;
    }

    @Override
    public List<PriceAlert> loadPriceAlertsByTicker(String ticker) {
        log.info("Loading price alerts for ticker {}", ticker);
        return List.of();
    }

    @Override
    public void addAlert(PriceAlert priceAlert) {
        log.info("Adding alert {}", priceAlert);
    }

    @Override
    public void removeAlert(UUID alertId) {
        log.info("Removing alert {}", alertId);
    }
}
