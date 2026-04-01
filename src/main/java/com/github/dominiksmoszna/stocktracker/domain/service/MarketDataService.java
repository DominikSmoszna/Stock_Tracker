package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.model.StockPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ProcessMarketDataUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MarketDataService implements ProcessMarketDataUseCase {

    private final CurrentPricePort currentPricePort;
    private final SaveCurrentPricePort saveCurrentPricePort;
    private final PriceHistoryPort priceHistoryPort;
    private final PriceUpdatePort priceUpdatePort;
    private final SendNotificationPort sendNotificationPort;
    private final LoadAlertPort loadAlertPort;

    @Override
    public StockPrice processPrice(StockPrice stockPrice) {
        // preserved for future percentage change calculation
        StockPrice currentPrice = Optional.ofNullable(currentPricePort.getCurrentPrice(stockPrice.getTicker()))
                .orElse(stockPrice);
        saveCurrentPricePort.saveCurrentPrice(stockPrice);
        List<PriceAlert> alerts = loadAlertPort.loadPriceAlertsByTicker(stockPrice.getTicker());
        alerts.stream().filter(alert ->
                ((alert.getType() == AlertType.ABOVE && (stockPrice.getPrice().compareTo(alert.getThreshold())>0)) ||
                (alert.getType() == AlertType.BELOW && (stockPrice.getPrice().compareTo(alert.getThreshold())<0)))
        ).forEach(alert -> sendNotificationPort.sendNotification(alert, stockPrice.getPrice()));
        priceUpdatePort.updateCurrentPrice(stockPrice);
        return stockPrice;
    }

    @Override
    public void recordPrice(StockPrice stockPrice) {
        priceHistoryPort.recordPrice(stockPrice);
    }
}
