package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.domain.model.HistoricalPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.in.GetPriceHistoryUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.GetPriceHistoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PriceHistoryService implements GetPriceHistoryUseCase {

    private final GetPriceHistoryPort getPriceHistoryPort;

    @Override
    public List<HistoricalPrice> getPriceHistory(String symbol, String interval, String range) {
        return getPriceHistoryPort.getPriceHistory(symbol, interval, range);
    }
}
