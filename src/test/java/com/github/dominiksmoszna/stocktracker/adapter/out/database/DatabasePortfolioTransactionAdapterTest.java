package com.github.dominiksmoszna.stocktracker.adapter.out.database;

import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.PortfolioTransactionEntity;
import com.github.dominiksmoszna.stocktracker.adapter.out.database.repository.PortfolioTransactionJpaRepository;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioTransaction;
import com.github.dominiksmoszna.stocktracker.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabasePortfolioTransactionAdapterTest {

    private static final UUID TX_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID POSITION_ID = UUID.randomUUID();
    private static final LocalDate DATE = LocalDate.of(2026, 1, 10);

    @Mock
    private PortfolioTransactionJpaRepository repository;

    private DatabasePortfolioTransactionAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DatabasePortfolioTransactionAdapter(repository);
    }

    @Test
    void shouldMapAndSaveTransaction() {
        PortfolioTransaction transaction = new PortfolioTransaction(
                TX_ID,
                USER_ID,
                TransactionType.BUY,
                DATE,
                POSITION_ID,
                "AAPL",
                new BigDecimal("10"),
                new BigDecimal("180.00"),
                Currency.getInstance("USD"),
                new BigDecimal("4.00"),
                new BigDecimal("7200.00")
        );

        adapter.saveTransaction(transaction);

        ArgumentCaptor<PortfolioTransactionEntity> captor = ArgumentCaptor.forClass(PortfolioTransactionEntity.class);
        verify(repository).save(captor.capture());

        PortfolioTransactionEntity saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(TX_ID);
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getType()).isEqualTo(TransactionType.BUY);
        assertThat(saved.getDate()).isEqualTo(DATE);
        assertThat(saved.getPositionId()).isEqualTo(POSITION_ID);
        assertThat(saved.getTicker()).isEqualTo("AAPL");
        assertThat(saved.getQuantity()).isEqualByComparingTo("10");
        assertThat(saved.getPricePerUnit()).isEqualByComparingTo("180.00");
        assertThat(saved.getCurrency()).isEqualTo("USD");
        assertThat(saved.getFxRateToPln()).isEqualByComparingTo("4.00");
        assertThat(saved.getAmountPln()).isEqualByComparingTo("7200.00");
    }

    @Test
    void shouldLoadTransactionsByUserAndNormalizeCurrency() {
        PortfolioTransactionEntity entity = new PortfolioTransactionEntity(
                TX_ID,
                USER_ID,
                TransactionType.DEPOSIT,
                DATE,
                null,
                null,
                null,
                null,
                " pln ",
                BigDecimal.ONE,
                new BigDecimal("5000.00")
        );
        when(repository.findAllByUserIdOrderByDateAsc(USER_ID)).thenReturn(List.of(entity));

        List<PortfolioTransaction> result = adapter.loadTransactionsByUser(USER_ID);

        assertThat(result).hasSize(1);
        PortfolioTransaction tx = result.getFirst();
        assertThat(tx.id()).isEqualTo(TX_ID);
        assertThat(tx.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(tx.currency()).isEqualTo(Currency.getInstance("PLN"));
        assertThat(tx.amountPln()).isEqualByComparingTo("5000.00");
    }

    @Test
    void shouldLoadTransactionsByPositionId() {
        PortfolioTransactionEntity entity = new PortfolioTransactionEntity(
                TX_ID,
                USER_ID,
                TransactionType.SELL,
                DATE,
                POSITION_ID,
                "AAPL",
                new BigDecimal("4"),
                new BigDecimal("200.00"),
                "USD",
                new BigDecimal("4.00"),
                new BigDecimal("3200.00")
        );
        when(repository.findAllByPositionIdOrderByDateAsc(POSITION_ID)).thenReturn(List.of(entity));

        List<PortfolioTransaction> result = adapter.loadTransactionsByPositionId(POSITION_ID);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().positionId()).isEqualTo(POSITION_ID);
        assertThat(result.getFirst().type()).isEqualTo(TransactionType.SELL);
    }

    @Test
    void shouldDelegateDeleteTransactionsByPositionId() {
        adapter.deleteTransactionsByPositionId(POSITION_ID);

        verify(repository).deleteAllByPositionId(POSITION_ID);
    }
}
