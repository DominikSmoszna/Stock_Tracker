package com.github.dominiksmoszna.stocktracker.domain.port.in;

import com.github.dominiksmoszna.stocktracker.domain.model.HistoricalPrice;

import java.util.List;

public interface GetPriceHistoryUseCase {
    List<HistoricalPrice> getPriceHistory(String symbol);
}
