package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;

import java.util.UUID;

public interface ManageAlertPort {
    void addAlert(PriceAlert priceAlert);
    void removeAlert(UUID alertId);
}
