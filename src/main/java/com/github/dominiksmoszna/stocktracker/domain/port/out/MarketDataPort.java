package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.StockQuote;

public interface MarketDataPort {
    StockQuote getQuote(String symbol);
}
