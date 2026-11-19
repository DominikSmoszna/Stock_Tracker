package com.github.dominiksmoszna.stocktracker.adapter.out.database;

import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.PortfolioTransactionEntity;
import com.github.dominiksmoszna.stocktracker.adapter.out.database.repository.PortfolioTransactionJpaRepository;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioTransaction;
import com.github.dominiksmoszna.stocktracker.domain.port.out.PortfolioTransactionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabasePortfolioTransactionAdapter implements PortfolioTransactionPort {

    private final PortfolioTransactionJpaRepository repository;

    @Override
    public void saveTransaction(PortfolioTransaction transaction) {
        PortfolioTransactionEntity entity = toEntity(transaction);
        repository.save(entity);
        log.info("Saving transaction {}", transaction);
    }

    @Override
    public List<PortfolioTransaction> loadTransactionsByUser(UUID userId) {
        log.info("Loading portfolio transactions for user {}", userId);
        return repository.findAllByUserIdOrderByDateAsc(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<PortfolioTransaction> loadTransactionsByPositionId(UUID positionId) {
        log.info("Loading portfolio transactions {}", positionId);
        return repository.findAllByPositionIdOrderByDateAsc(positionId).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteTransactionsByPositionId(UUID positionId) {
        repository.deleteAllByPositionId(positionId);
        log.info("Deleting transactions for position {}", positionId);
    }

    private PortfolioTransactionEntity toEntity(PortfolioTransaction transaction) {
        return new PortfolioTransactionEntity(
                transaction.id(),
                transaction.userId(),
                transaction.type(),
                transaction.date(),
                transaction.positionId(),
                transaction.ticker(),
                transaction.quantity(),
                transaction.pricePerUnit(),
                transaction.currency().getCurrencyCode(),
                transaction.fxRateToPln(),
                transaction.amountPln()
        );
    }

    private PortfolioTransaction toDomain(PortfolioTransactionEntity transaction) {
        return new PortfolioTransaction(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getType(),
                transaction.getDate(),
                transaction.getPositionId(),
                transaction.getTicker(),
                transaction.getQuantity(),
                transaction.getPricePerUnit(),
                Currency.getInstance(transaction.getCurrency().trim().toUpperCase()),
                transaction.getFxRateToPln(),
                transaction.getAmountPln()
        );
    }
}
