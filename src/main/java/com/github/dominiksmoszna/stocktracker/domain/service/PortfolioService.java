package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.common.exception.InsufficientCashException;
import com.github.dominiksmoszna.stocktracker.common.exception.InvalidPortfolioOperationException;
import com.github.dominiksmoszna.stocktracker.common.exception.PortfolioPositionNotFoundException;
import com.github.dominiksmoszna.stocktracker.common.exception.SymbolNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.*;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ManagePortfolioUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@RequiredArgsConstructor
public class PortfolioService implements ManagePortfolioUseCase {

    private final LoadPortfolioPort loadPortfolioPort;
    private final ManagePortfolioPort managePortfolioPort;
    private final InstrumentInfoPort instrumentInfoPort;
    private final PortfolioValuationPort portfolioValuationPort;
    private final PortfolioTransactionPort portfolioTransactionPort;
    private final ExchangeRatePort exchangeRatePort;
    private static final Currency PLN = Currency.getInstance("PLN");
    private static final int FX_LOOKBACK_DAYS = 7;

    private BigDecimal currentBalance(UUID userId){
        List<PortfolioTransaction> transactions = portfolioTransactionPort.loadTransactionsByUser(userId);
        return transactions.stream().map(this::signedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal signedAmount(PortfolioTransaction transaction) {
        BigDecimal amount = transaction.amountPln();

        return switch (transaction.type()) {
            case TransactionType.DEPOSIT, TransactionType.SELL -> amount;
            case TransactionType.WITHDRAWAL,TransactionType.BUY -> amount.negate();
        };
    }

    private BigDecimal resolveFxRate(Currency currency, LocalDate date) {
        if (PLN.equals(currency)) {
            return BigDecimal.ONE;
        }
        LocalDate from = date.minusDays(FX_LOOKBACK_DAYS);
        return exchangeRatePort.getRates(currency, PLN, from, date).stream()
                .filter(p->!p.date().isAfter(date))
                .max(Comparator.comparing(ExchangeRatePoint::date))
                .map(ExchangeRatePoint::rate)
                .orElseThrow(() -> new IllegalStateException("Brak kursu FX %s/PLN w okresie %s - %s".formatted(currency.getCurrencyCode(), from, date)));
    }

    private Currency resolveCurrency(String ticker) {
        return instrumentInfoPort.loadInstrument(ticker)
                .map(InstrumentInfo::currency)
                .orElseThrow(() -> new SymbolNotFoundException("Instrument not found: " + ticker));
    }

    @Override
    @Transactional
    public PortfolioPosition addPosition(UUID userId, String ticker, BigDecimal quantity, BigDecimal purchasePrice, LocalDate purchaseDate, Currency currency) {
        String normalizedTicker = ticker.trim().toUpperCase();
        Currency resolvedCurrency = currency != null ? currency : resolveCurrency(normalizedTicker);

        BigDecimal fxRate = resolveFxRate(resolvedCurrency, purchaseDate);
        BigDecimal costPln = quantity.multiply(purchasePrice).multiply(fxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal balance = currentBalance(userId);
        if (balance.compareTo(costPln) < 0) {
            throw new InsufficientCashException("purchasePrice cannot be larger than current portfolio balance");
        }

        UUID positionId = UUID.randomUUID();
        PortfolioPosition portfolioPosition = new PortfolioPosition(
                positionId,
                userId,
                normalizedTicker,
                quantity,
                purchasePrice,
                purchaseDate,
                resolvedCurrency,
                quantity,
                PositionStatus.OPEN,
                null
        );
        managePortfolioPort.savePosition(portfolioPosition);

        PortfolioTransaction transaction = new PortfolioTransaction(
                UUID.randomUUID(),
                userId,
                TransactionType.BUY,
                purchaseDate,
                positionId,
                normalizedTicker,
                quantity,
                purchasePrice,
                resolvedCurrency,
                fxRate,
                costPln
        );
        portfolioTransactionPort.saveTransaction(transaction);
        return portfolioPosition;
    }

    @Override
    @Transactional
    public void removePosition(UUID positionId) {
        Optional.ofNullable(loadPortfolioPort.loadPosition(positionId))
                .ifPresentOrElse(portfolioPosition -> {
                    portfolioTransactionPort.deleteTransactionsByPositionId(positionId);
                    managePortfolioPort.deletePosition(positionId);
                    },
                        () -> {throw new PortfolioPositionNotFoundException("Position not found");});
    }

    @Override
    public List<PortfolioPosition> getPositions(UUID userId) {return loadPortfolioPort.loadPositions(userId);}

    @Override
    public PortfolioValuationResult getPortfolioValuation(UUID userId, Currency targetCurrency, LocalDate endDate) {
        Currency resolvedCurrency = targetCurrency != null ? targetCurrency : PLN;
        List<PortfolioPosition> positions = loadPortfolioPort.loadPositions(userId);
        return portfolioValuationPort.getHistoricalValuation(positions, resolvedCurrency, endDate);
    }

    @Override
    public PortfolioTransaction depositCash(UUID userId, BigDecimal amountPln, LocalDate date) {
        if (amountPln == null || amountPln.signum() <= 0) {
            throw new InvalidPortfolioOperationException("cannot proceed with this action");
        }

        UUID id = UUID.randomUUID();
        PortfolioTransaction transaction = new PortfolioTransaction(id, userId, TransactionType.DEPOSIT, date, null, null, null, null, PLN, BigDecimal.ONE, amountPln);
        portfolioTransactionPort.saveTransaction(transaction);
        return transaction;
    }

    @Override
    public PortfolioTransaction withdrawCash(UUID userId, BigDecimal amountPln, LocalDate date) {
        if (amountPln == null || amountPln.signum()<=0) {
            throw new InvalidPortfolioOperationException("cannot proceed with this action");
        }
        BigDecimal balance = currentBalance(userId);

        if (balance.compareTo(amountPln) < 0) {
            throw new InsufficientCashException("withdrawal amount cannot be larger than current portfolio balance");
        }

        UUID id = UUID.randomUUID();
        PortfolioTransaction transaction = new PortfolioTransaction(
                id,
                userId,
                TransactionType.WITHDRAWAL,
                date,
                null,
                null,
                null,
                null,
                PLN,
                BigDecimal.ONE,
                amountPln
        );
        portfolioTransactionPort.saveTransaction(transaction);
        return transaction;
    }

    @Override
    public PortfolioCashBalance getCashBalance(UUID userId) {
        List<PortfolioTransaction> transactions = portfolioTransactionPort.loadTransactionsByUser(userId);
        BigDecimal cashBalance = transactions.stream().map(this::signedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netDeposit = transactions.stream().filter(tx -> tx.type() == TransactionType.DEPOSIT || tx.type() == TransactionType.WITHDRAWAL).map(this::signedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PortfolioCashBalance(userId, PLN, cashBalance, netDeposit);
    }

    @Override
    public List<PortfolioTransaction> getTransactions(UUID userId) {
        return portfolioTransactionPort.loadTransactionsByUser(userId);
    }

    @Override
    @Transactional
    public PortfolioPosition sellPosition(UUID positionId, BigDecimal sellQuantity, BigDecimal sellPrice, LocalDate sellDate) {
        PortfolioPosition portfolioPosition = Optional.ofNullable(loadPortfolioPort.loadPosition(positionId))
                .orElseThrow(() -> new PortfolioPositionNotFoundException("Position not found"));

        if (portfolioPosition.status().equals(PositionStatus.OPEN) && sellQuantity != null && sellQuantity.compareTo(BigDecimal.ZERO) > 0 && sellQuantity.compareTo(portfolioPosition.quantity()) <= 0 && sellPrice != null && sellPrice.compareTo(BigDecimal.ZERO) > 0 && sellDate != null && !sellDate.isBefore((portfolioPosition.purchaseDate()))) {
            BigDecimal fxRate = resolveFxRate(portfolioPosition.currency(), sellDate);
            BigDecimal proceedsPln = sellQuantity.multiply(sellPrice).multiply(fxRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal newQuantity = portfolioPosition.quantity().subtract(sellQuantity);
            PositionStatus status = newQuantity.compareTo(BigDecimal.ZERO) == 0 ? PositionStatus.CLOSED : PositionStatus.OPEN;
            PortfolioPosition position = new PortfolioPosition(
                    portfolioPosition.id(),
                    portfolioPosition.userId(),
                    portfolioPosition.ticker(),
                    newQuantity,
                    portfolioPosition.purchasePrice(),
                    portfolioPosition.purchaseDate(),
                    portfolioPosition.currency(),
                    portfolioPosition.initialQuantity(),
                    status,
                    status.compareTo(PositionStatus.OPEN) == 0 ? null : sellDate
            );
            managePortfolioPort.savePosition(position);

            PortfolioTransaction transaction = new PortfolioTransaction(
                    UUID.randomUUID(),
                    portfolioPosition.userId(),
                    TransactionType.SELL,
                    sellDate,
                    portfolioPosition.id(),
                    portfolioPosition.ticker(),
                    sellQuantity,
                    sellPrice,
                    portfolioPosition.currency(),
                    fxRate,
                    proceedsPln
            );
            portfolioTransactionPort.saveTransaction(transaction);
            return position;
        }else {
            throw  new InvalidPortfolioOperationException("cannot proceed with this action");
        }
    }
}
