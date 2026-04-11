package com.github.dominiksmoszna.stocktracker.adapter.out.websocket;

import com.github.dominiksmoszna.stocktracker.domain.model.StockPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.out.PriceUpdatePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketPriceUpdateAdapter implements PriceUpdatePort {
    @Override
    public void updateCurrentPrice(StockPrice stockPrice) {
        log.info("Price update received, Ticker: {}, Current price: {}", stockPrice.getTicker(), stockPrice.getPrice());
    }
}
