package com.github.dominiksmoszna.stocktracker.adapter.out.database;

import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.PortfolioPositionEntity;
import com.github.dominiksmoszna.stocktracker.adapter.out.database.repository.PortfolioPositionJpaRepository;
import com.github.dominiksmoszna.stocktracker.common.exception.PortfolioPositionNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadPortfolioPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ManagePortfolioPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabasePortfolioAdapter implements LoadPortfolioPort, ManagePortfolioPort {

    private final PortfolioPositionJpaRepository repository;

    @Override
    public List<PortfolioPosition> loadPositions(UUID userId) {
        log.info("Loading portfolio positions for user {}", userId);
        return repository.findAllByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public PortfolioPosition loadPosition(UUID positionId) {
        log.info("Loading portfolio position {}", positionId);
        return repository.findById(positionId).map(this::toDomain).orElse(null);
    }

    @Override
    public void savePosition(PortfolioPosition position) {
        PortfolioPositionEntity entity = toEntity(position);
        repository.save(entity);
        log.info("Saving position {}", position);
    }

    @Override
    public void deletePosition(UUID positionId) {
        if (!repository.existsById(positionId)) {
            throw new PortfolioPositionNotFoundException("No position with id " + positionId + " found");
        }
        repository.deleteById(positionId);
        log.info("Deleting position {}", positionId);
    }

    private PortfolioPositionEntity toEntity(PortfolioPosition position) {
        return new PortfolioPositionEntity(
                position.id(),
                position.userId(),
                position.ticker(),
                position.quantity(),
                position.purchasePrice(),
                position.purchaseDate()
        );
    }

    private PortfolioPosition toDomain(PortfolioPositionEntity position) {
        return new PortfolioPosition(
                position.getId(),
                position.getUserId(),
                position.getTicker(),
                position.getQuantity(),
                position.getPurchasePrice(),
                position.getPurchaseDate()
        );
    }
}
