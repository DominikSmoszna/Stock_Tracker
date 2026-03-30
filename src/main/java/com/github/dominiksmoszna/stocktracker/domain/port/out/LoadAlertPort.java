package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;

import java.util.List;

public interface LoadAlertPort {
    List<PriceAlert> getPriceAlerts(String ticker);
}
