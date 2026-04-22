package com.github.dominiksmoszna.stocktracker.adapter.out.database;

import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.StockPriceEntity;
import com.github.dominiksmoszna.stocktracker.adapter.out.database.repository.StockPriceJpaRepository;
import com.github.dominiksmoszna.stocktracker.domain.model.StockPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.out.PriceHistoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabasePriceHistoryAdapter implements PriceHistoryPort {

    private final StockPriceJpaRepository repository;

    @Override
    public void recordPrice(StockPrice stockPrice) {
        StockPriceEntity entity = toEntity(stockPrice);
        repository.save(entity);
        log.info("Recording price {}", stockPrice);
    }

    private StockPriceEntity toEntity(StockPrice stockPrice) {
        return new StockPriceEntity(
                UUID.randomUUID(),
                stockPrice.ticker(),
                stockPrice.price(),
                stockPrice.timestamp()
        );
    }

}
