package com.github.dominiksmoszna.stocktracker.adapter.out.cache;

import com.github.dominiksmoszna.stocktracker.domain.model.StockPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.out.CurrentPricePort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.SaveCurrentPricePort;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryStockCache implements CurrentPricePort, SaveCurrentPricePort {

    ConcurrentHashMap<String, StockPrice> stockPrices = new ConcurrentHashMap<>();

    @Override
    public StockPrice getCurrentPrice(String ticker) {
        return stockPrices.get(ticker);
    }

    @Override
    public void saveCurrentPrice(StockPrice stockPrice) {
        stockPrices.put(stockPrice.ticker(), stockPrice);
    }
}
