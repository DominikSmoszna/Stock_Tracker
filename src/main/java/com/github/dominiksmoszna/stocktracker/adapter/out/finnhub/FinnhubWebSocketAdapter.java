package com.github.dominiksmoszna.stocktracker.adapter.out.finnhub;

import com.github.dominiksmoszna.stocktracker.domain.port.in.ProcessMarketDataUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadTrackedSymbolsPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.StockSubscriptionPort;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@Component
public class FinnhubWebSocketAdapter implements StockSubscriptionPort {

    private final FinnhubWebSocketClient finnhubWebSocketClient;
    private final LoadTrackedSymbolsPort loadTrackedSymbolsPort;
    private final String apiKey;
    private final boolean websocketEnabled;

    @Autowired
    public FinnhubWebSocketAdapter(
            @Value("${finnhub.api.key:}") String apiKey,
            @Value("${finnhub.websocket.enabled:true}") boolean websocketEnabled,
            LoadTrackedSymbolsPort loadTrackedSymbolsPort,
            ProcessMarketDataUseCase processMarketDataUseCase
    ) {
        this.apiKey = apiKey;
        this.websocketEnabled = websocketEnabled;
        this.loadTrackedSymbolsPort = loadTrackedSymbolsPort;
        this.finnhubWebSocketClient = new FinnhubWebSocketClient(
                URI.create("wss://ws.finnhub.io?token=" + apiKey),
                processMarketDataUseCase,
                this::subscribeAllTrackedSymbols
        );
    }

    FinnhubWebSocketAdapter(
            String apiKey,
            boolean websocketEnabled,
            LoadTrackedSymbolsPort loadTrackedSymbolsPort,
            FinnhubWebSocketClient finnhubWebSocketClient
    ) {
        this.apiKey = apiKey;
        this.websocketEnabled = websocketEnabled;
        this.loadTrackedSymbolsPort = loadTrackedSymbolsPort;
        this.finnhubWebSocketClient = finnhubWebSocketClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        if (!websocketEnabled) {
            log.info("Finnhub WebSocket is disabled (finnhub.websocket.enabled=false)");
            return;
        }
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Finnhub API key is blank, skipping WebSocket connection");
            return;
        }
        log.info("Initiating non-blocking Finnhub WebSocket connection");
        finnhubWebSocketClient.connect();
    }

    @PreDestroy
    public void disconnect() {
        finnhubWebSocketClient.shutdown();
    }

    void subscribeAllTrackedSymbols() {
        loadTrackedSymbolsPort.loadAllSymbols().forEach(this::subscribe);
    }

    @Override
    public void subscribe(String ticker) {
        if (!finnhubWebSocketClient.isOpen()) {
            log.warn("Finnhub WebSocket is not open, skipping immediate subscribe for: {} (will subscribe on connect)", ticker);
            return;
        }
        finnhubWebSocketClient.send(String.format("{\"type\":\"subscribe\",\"symbol\":\"%s\"}", ticker));
        log.info("Subscribing to: {}", ticker);
    }

    @Override
    public void unsubscribe(String ticker) {
        if (!finnhubWebSocketClient.isOpen()) {
            log.warn("Finnhub WebSocket is not open, skipping unsubscribe for: {}", ticker);
            return;
        }
        finnhubWebSocketClient.send(String.format("{\"type\":\"unsubscribe\",\"symbol\":\"%s\"}", ticker));
    }
}
