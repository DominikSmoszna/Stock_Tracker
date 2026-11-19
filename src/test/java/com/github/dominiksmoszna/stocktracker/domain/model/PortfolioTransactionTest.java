package com.github.dominiksmoszna.stocktracker.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortfolioTransactionTest {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID POSITION_ID = UUID.randomUUID();
    private static final LocalDate DATE = LocalDate.of(2026, 1, 10);
    private static final Currency PLN = Currency.getInstance("PLN");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void shouldCreateDepositAndWithdrawalWithoutPositionFields() {
        PortfolioTransaction deposit = new PortfolioTransaction(
                ID, USER_ID, TransactionType.DEPOSIT, DATE,
                null, null, null, null,
                PLN, BigDecimal.ONE, new BigDecimal("5000.00")
        );
        PortfolioTransaction withdrawal = new PortfolioTransaction(
                ID, USER_ID, TransactionType.WITHDRAWAL, DATE,
                null, null, null, null,
                PLN, BigDecimal.ONE, new BigDecimal("1000.00")
        );

        assertThat(deposit.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(deposit.positionId()).isNull();
        assertThat(withdrawal.type()).isEqualTo(TransactionType.WITHDRAWAL);
    }

    @Test
    void shouldCreateBuyAndSellWhenTradeFieldsAreValid() {
        PortfolioTransaction buy = new PortfolioTransaction(
                ID, USER_ID, TransactionType.BUY, DATE,
                POSITION_ID, "AAPL", new BigDecimal("10"), new BigDecimal("180.00"),
                USD, new BigDecimal("4.00"), new BigDecimal("7200.00")
        );

        assertThat(buy.ticker()).isEqualTo("AAPL");
        assertThat(buy.amountPln()).isEqualByComparingTo("7200.00");
    }

    @Test
    void shouldRejectNullRequiredFields() {
        assertThatThrownBy(() -> new PortfolioTransaction(
                null, USER_ID, TransactionType.DEPOSIT, DATE,
                null, null, null, null,
                PLN, BigDecimal.ONE, new BigDecimal("5000.00")
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PortfolioTransaction(
                ID, null, TransactionType.DEPOSIT, DATE,
                null, null, null, null,
                PLN, BigDecimal.ONE, new BigDecimal("5000.00")
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PortfolioTransaction(
                ID, USER_ID, null, DATE,
                null, null, null, null,
                PLN, BigDecimal.ONE, new BigDecimal("5000.00")
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PortfolioTransaction(
                ID, USER_ID, TransactionType.DEPOSIT, null,
                null, null, null, null,
                PLN, BigDecimal.ONE, new BigDecimal("5000.00")
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PortfolioTransaction(
                ID, USER_ID, TransactionType.DEPOSIT, DATE,
                null, null, null, null,
                null, BigDecimal.ONE, new BigDecimal("5000.00")
        )).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNonPositiveFxRateOrAmountPln() {
        assertThatThrownBy(() -> new PortfolioTransaction(
                ID, USER_ID, TransactionType.DEPOSIT, DATE,
                null, null, null, null,
                PLN, BigDecimal.ZERO, new BigDecimal("5000.00")
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new PortfolioTransaction(
                ID, USER_ID, TransactionType.DEPOSIT, DATE,
                null, null, null, null,
                PLN, BigDecimal.ONE, new BigDecimal("-10.00")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBuyOrSellWithMissingOrInvalidTradeFields() {
        assertThatThrownBy(() -> new PortfolioTransaction(
                ID, USER_ID, TransactionType.BUY, DATE,
                null, "AAPL", BigDecimal.TEN, BigDecimal.TEN,
                USD, BigDecimal.ONE, BigDecimal.TEN
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PortfolioTransaction(
                ID, USER_ID, TransactionType.BUY, DATE,
                POSITION_ID, "   ", BigDecimal.TEN, BigDecimal.TEN,
                USD, BigDecimal.ONE, BigDecimal.TEN
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new PortfolioTransaction(
                ID, USER_ID, TransactionType.SELL, DATE,
                POSITION_ID, "AAPL", BigDecimal.ZERO, BigDecimal.TEN,
                USD, BigDecimal.ONE, BigDecimal.TEN
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new PortfolioTransaction(
                ID, USER_ID, TransactionType.SELL, DATE,
                POSITION_ID, "AAPL", BigDecimal.TEN, BigDecimal.ZERO,
                USD, BigDecimal.ONE, BigDecimal.TEN
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
