package com.github.dominiksmoszna.stocktracker.adapter.out.cache;

import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ActiveAlertCachePort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadAlertPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryActiveAlertCache implements ActiveAlertCachePort {

    private final LoadAlertPort loadAlertPort;

    private final ConcurrentHashMap<String, Map<UUID, PriceAlert>> activeAlertsByTicker = new ConcurrentHashMap<>();

    /**
     * Warm-up runs before the application is ready, i.e. before the Finnhub WebSocket starts streaming ticks.
     */
    @PostConstruct
    void loadActiveAlerts() {
        List<PriceAlert> activeAlerts = loadAlertPort.loadActivePriceAlerts();
        activeAlerts.forEach(this::addActiveAlert);
        log.info("Loaded {} active price alerts into memory", activeAlerts.size());
    }

    @Override
    public List<PriceAlert> getActiveAlerts(String ticker) {
        Map<UUID, PriceAlert> alerts = activeAlertsByTicker.get(ticker);
        return alerts == null ? List.of() : List.copyOf(alerts.values());
    }

    @Override
    public void addActiveAlert(PriceAlert priceAlert) {
        if (priceAlert.triggered()) {
            return;
        }
        activeAlertsByTicker
                .computeIfAbsent(priceAlert.ticker(), ticker -> new ConcurrentHashMap<>())
                .put(priceAlert.id(), priceAlert);
    }

    @Override
    public boolean removeActiveAlert(PriceAlert priceAlert) {
        boolean[] removed = {false};
        activeAlertsByTicker.computeIfPresent(priceAlert.ticker(), (ticker, alerts) -> {
            removed[0] = alerts.remove(priceAlert.id()) != null;
            return alerts.isEmpty() ? null : alerts;
        });
        return removed[0];
    }
}
