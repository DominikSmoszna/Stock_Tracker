package com.github.dominiksmoszna.stocktracker.domain.port.in;

import com.github.dominiksmoszna.stocktracker.domain.model.StockQuote;

public interface GetMarketQuoteUseCase {
    StockQuote getQuote(String symbol);
}
