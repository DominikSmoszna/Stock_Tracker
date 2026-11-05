package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;

import java.time.Instant;
import java.util.UUID;

public interface ManageAlertPort {
    void addAlert(PriceAlert priceAlert);
    void removeAlert(UUID alertId);

    /**
     * Persists the TRIGGERED state of an alert.
     *
     * @return true if the alert existed and was still active, false otherwise
     */
    boolean markAlertTriggered(UUID alertId, Instant triggeredAt);
}
