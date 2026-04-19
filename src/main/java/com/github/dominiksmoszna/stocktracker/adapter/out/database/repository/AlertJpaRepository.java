package com.github.dominiksmoszna.stocktracker.adapter.out.database.repository;

import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertJpaRepository extends JpaRepository<AlertEntity, UUID> {

    List<AlertEntity> findAllByUserId(UUID userId);

    List<AlertEntity> findAllByUserIdAndTicker(UUID userId, String ticker);

    List<AlertEntity> findAllByTicker(String ticker);
}
