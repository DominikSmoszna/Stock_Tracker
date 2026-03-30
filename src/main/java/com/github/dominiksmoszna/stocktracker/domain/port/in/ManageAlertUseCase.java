package com.github.dominiksmoszna.stocktracker.domain.port.in;

import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ManageAlertUseCase {

    PriceAlert addAlert(String ticker, BigDecimal threshold, AlertType type);
    void removeAlert(UUID alertId);
    List<PriceAlert> getPriceAlerts(UUID userId);
}
