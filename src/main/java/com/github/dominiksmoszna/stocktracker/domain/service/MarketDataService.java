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
        StockPrice currentPrice = Optional.ofNullable(currentPricePort.getCurrentPrice(stockPrice.ticker()))
                .orElse(stockPrice);
        saveCurrentPricePort.saveCurrentPrice(stockPrice);
        List<PriceAlert> alerts = loadAlertPort.loadPriceAlertsByTicker(stockPrice.ticker());
        alerts.stream().filter(alert ->
                ((alert.type() == AlertType.ABOVE && (stockPrice.price().compareTo(alert.threshold())>0)) ||
                (alert.type() == AlertType.BELOW && (stockPrice.price().compareTo(alert.threshold())<0)))
        ).forEach(alert -> sendNotificationPort.sendNotification(alert, stockPrice.price()));
        priceUpdatePort.updateCurrentPrice(stockPrice);
        return stockPrice;
    }

    @Override
    public void recordPrice(StockPrice stockPrice) {
        priceHistoryPort.recordPrice(stockPrice);
    }
}
