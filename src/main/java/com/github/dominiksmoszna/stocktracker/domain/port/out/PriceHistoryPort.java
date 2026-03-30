package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.StockPrice;

public interface PriceHistoryPort {
    void recordPrice(StockPrice stockPrice);
}
