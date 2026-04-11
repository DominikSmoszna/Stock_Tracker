package com.github.dominiksmoszna.stocktracker.adapter.out.database;

import com.github.dominiksmoszna.stocktracker.domain.model.StockPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.out.PriceHistoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabasePriceHistoryAdapter implements PriceHistoryPort {
    @Override
    public void recordPrice(StockPrice stockPrice) {
        log.info("Recording price {}", stockPrice);
    }
}
