package com.github.dominiksmoszna.stocktracker.adapter.out.database.repository;
import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.PortfolioTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface PortfolioTransactionJpaRepository extends JpaRepository<PortfolioTransactionEntity, UUID> {
    List<PortfolioTransactionEntity> findAllByUserIdOrderByDateAsc(UUID userId);
    List<PortfolioTransactionEntity> findAllByPositionIdOrderByDateAsc(UUID positionId);
    @Transactional
    @Modifying
    void deleteAllByPositionId(UUID positionId);
}
