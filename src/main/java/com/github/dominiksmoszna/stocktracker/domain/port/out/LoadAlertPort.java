package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;

import java.util.List;
import java.util.UUID;

public interface LoadAlertPort {
    List<PriceAlert> loadPriceAlerts(UUID userId);
    List<PriceAlert> loadPriceAlertsByTicker(UUID userId, String ticker);
    PriceAlert loadPriceAlert(UUID alertId);

}
