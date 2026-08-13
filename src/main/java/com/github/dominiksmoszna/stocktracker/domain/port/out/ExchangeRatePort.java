package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.ExchangeRatePoint;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

public interface ExchangeRatePort {
    List<ExchangeRatePoint> getRates(
            Currency from,
            Currency to,
            LocalDate startDate,
            LocalDate endDate
    );
}
