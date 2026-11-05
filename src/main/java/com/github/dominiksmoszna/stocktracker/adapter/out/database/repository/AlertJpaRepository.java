package com.github.dominiksmoszna.stocktracker.adapter.out.database.repository;

import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AlertJpaRepository extends JpaRepository<AlertEntity, UUID> {

    List<AlertEntity> findAllByUserId(UUID userId);

    List<AlertEntity> findAllByUserIdAndTicker(UUID userId, String ticker);

    List<AlertEntity> findAllByTriggeredFalse();

    @Transactional
    @Modifying
    @Query("update AlertEntity a set a.triggered = true, a.triggeredAt = :triggeredAt " +
            "where a.id = :id and a.triggered = false")
    int markTriggered(@Param("id") UUID id, @Param("triggeredAt") Instant triggeredAt);
}
