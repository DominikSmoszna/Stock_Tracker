package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.HistoricalPrice;

import java.util.List;

public interface GetPriceHistoryPort {
    List<HistoricalPrice> getPriceHistory(String symbol);
}
