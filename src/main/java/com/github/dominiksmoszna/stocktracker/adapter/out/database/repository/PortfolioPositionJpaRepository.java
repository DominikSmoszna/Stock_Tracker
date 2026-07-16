package com.github.dominiksmoszna.stocktracker.adapter.out.database.repository;

import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.PortfolioPositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PortfolioPositionJpaRepository extends JpaRepository<PortfolioPositionEntity, UUID> {
    List<PortfolioPositionEntity> findAllByUserId(UUID userId);
}
