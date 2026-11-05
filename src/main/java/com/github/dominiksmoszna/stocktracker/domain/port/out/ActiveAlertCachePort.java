package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;

import java.util.List;

/**
 * In-memory view of alerts that have not been triggered yet, used on the hot path of price processing
 * so that incoming ticks do not hit the database.
 */
public interface ActiveAlertCachePort {

    List<PriceAlert> getActiveAlerts(String ticker);

    void addActiveAlert(PriceAlert priceAlert);

    /**
     * Removes the alert from the cache.
     *
     * @return true if the alert was present and has been removed by this call
     */
    boolean removeActiveAlert(PriceAlert priceAlert);
}
