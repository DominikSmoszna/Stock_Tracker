package com.github.dominiksmoszna.stocktracker.adapter.out.database.repository;

import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.WatchlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WatchlistJpaRepository extends JpaRepository<WatchlistEntity, UUID> {

    List<WatchlistEntity> findAllByUserId(UUID userId);

    @Query("SELECT COUNT(w)>0 FROM WatchlistEntity w JOIN w.tickers t WHERE t = :ticker")
    boolean existsByTicker(@Param("ticker") String ticker);

}
