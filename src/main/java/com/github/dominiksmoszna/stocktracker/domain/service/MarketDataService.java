package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.model.StockPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ProcessMarketDataUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.*;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class MarketDataService implements ProcessMarketDataUseCase {

    private final CurrentPricePort currentPricePort;
    private final SaveCurrentPricePort saveCurrentPricePort;
    private final PriceHistoryPort priceHistoryPort;
    private final PriceUpdatePort priceUpdatePort;
    private final SendNotificationPort sendNotificationPort;
    private final ActiveAlertCachePort activeAlertCachePort;
    private final ManageAlertPort manageAlertPort;

    @Override
    public StockPrice processPrice(StockPrice stockPrice) {
        // preserved for future percentage change calculation
        StockPrice currentPrice = Optional.ofNullable(currentPricePort.getCurrentPrice(stockPrice.ticker()))
                .orElse(stockPrice);
        saveCurrentPricePort.saveCurrentPrice(stockPrice);
        evaluateAlerts(stockPrice);
        priceUpdatePort.updateCurrentPrice(stockPrice);
        return stockPrice;
    }

    @Override
    public void recordPrice(StockPrice stockPrice) {
        priceHistoryPort.recordPrice(stockPrice);
    }

    private void evaluateAlerts(StockPrice stockPrice) {
        for (PriceAlert alert : activeAlertCachePort.getActiveAlerts(stockPrice.ticker())) {
            if (!alert.isTriggeredBy(stockPrice.price())) {
                continue;
            }
            // Claim the alert first, so that it fires exactly once even if ticks are processed concurrently.
            if (!activeAlertCachePort.removeActiveAlert(alert)) {
                continue;
            }
            Instant triggeredAt = Optional.ofNullable(stockPrice.timestamp()).orElseGet(Instant::now);
            PriceAlert triggeredAlert = alert.markTriggered(triggeredAt);

            boolean persisted;
            try {
                persisted = manageAlertPort.markAlertTriggered(alert.id(), triggeredAt);
            } catch (RuntimeException e) {
                // Persistence failed (logged by the adapter) - put the alert back so it can fire on a later tick.
                activeAlertCachePort.addActiveAlert(alert);
                continue;
            }
            // false = alert was deleted or already triggered in the meantime
            if (persisted) {
                sendNotificationPort.sendNotification(triggeredAlert, stockPrice.price());
            }
        }
    }
}
