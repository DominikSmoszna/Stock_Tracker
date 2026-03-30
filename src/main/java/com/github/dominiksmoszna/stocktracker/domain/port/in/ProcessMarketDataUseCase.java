package com.github.dominiksmoszna.stocktracker.domain.port.in;

import com.github.dominiksmoszna.stocktracker.domain.model.StockPrice;

public interface ProcessMarketDataUseCase {

    StockPrice processPrice(StockPrice stockPrice);
    void recordPrice(StockPrice stockPrice);
}
