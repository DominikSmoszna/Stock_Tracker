package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.domain.model.StockQuote;
import com.github.dominiksmoszna.stocktracker.domain.port.in.GetMarketQuoteUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.MarketDataPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MarketQuoteService implements GetMarketQuoteUseCase {

    private final MarketDataPort marketDataPort;

    @Override
    public StockQuote getQuote(String symbol) {
        return marketDataPort.getQuote(symbol);
    }
}
