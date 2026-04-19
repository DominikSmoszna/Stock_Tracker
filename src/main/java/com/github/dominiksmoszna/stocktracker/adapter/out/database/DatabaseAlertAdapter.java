package com.github.dominiksmoszna.stocktracker.adapter.out.database;

import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.AlertEntity;
import com.github.dominiksmoszna.stocktracker.adapter.out.database.repository.AlertJpaRepository;
import com.github.dominiksmoszna.stocktracker.common.exception.PriceAlertNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadAlertPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ManageAlertPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseAlertAdapter implements LoadAlertPort, ManageAlertPort {

    private final AlertJpaRepository repository;

    @Override
    public List<PriceAlert> loadPriceAlerts(UUID userId) {
        log.info("Loading price alerts for user {}", userId);
        return repository.findAllByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<PriceAlert> loadPriceAlertsByTicker(UUID userId, String ticker) {
        log.info("Loading price alerts for user {} by ticker {}", userId, ticker);
        return repository.findAllByUserIdAndTicker(userId, ticker).stream().map(this::toDomain).toList();
    }

    @Override
    public PriceAlert loadPriceAlert(UUID alertId) {
        log.info("Loading price alert {}", alertId);
        return repository.findById(alertId).map(this::toDomain).orElse(null);
    }

    @Override
    public List<PriceAlert> loadPriceAlertsByTicker(String ticker) {
        log.info("Loading price alerts for ticker {}", ticker);
        return repository.findAllByTicker(ticker).stream().map(this::toDomain).toList();
    }

    @Override
    public void addAlert(PriceAlert priceAlert) {
        AlertEntity entity = toEntity(priceAlert);
        repository.save(entity);
        log.info("Adding alert {}", priceAlert);
    }

    @Override
    public void removeAlert(UUID alertId) {
        if(!repository.existsById(alertId)) {
            throw new PriceAlertNotFoundException("No alert with id " + alertId + " found");
        }
        repository.deleteById(alertId);
        log.info("Removing alert {}", alertId);
    }

    private AlertEntity toEntity(PriceAlert alert) {
        return new AlertEntity(
                alert.getId(),
                alert.getUserId(),
                alert.getTicker(),
                alert.getThreshold(),
                alert.getType()
        );
    }

    private PriceAlert toDomain(AlertEntity alert) {
        return new PriceAlert(
                alert.getId(),
                alert.getUserId(),
                alert.getTicker(),
                alert.getThreshold(),
                alert.getType()
        );
    }


}
