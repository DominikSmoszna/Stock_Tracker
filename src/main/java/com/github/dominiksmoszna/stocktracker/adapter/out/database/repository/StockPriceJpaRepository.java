package com.github.dominiksmoszna.stocktracker.adapter.out.database.repository;

import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.StockPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockPriceJpaRepository extends JpaRepository<StockPriceEntity, UUID> {
}

