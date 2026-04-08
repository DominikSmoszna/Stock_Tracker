package com.github.dominiksmoszna.stocktracker.adapter.out.finnhub;

import com.github.dominiksmoszna.stocktracker.domain.port.in.ProcessMarketDataUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadTrackedSymbolsPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.StockSubscriptionPort;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;


@Component
public class FinnhubWebSocketAdapter implements StockSubscriptionPort {

    FinnhubWebSocketClient finnhubWebSocketClient;
    LoadTrackedSymbolsPort loadTrackedSymbolsPort;

    private final String apiKey;

    public FinnhubWebSocketAdapter(@Value("${finnhub.api.key}") String apiKey, LoadTrackedSymbolsPort loadTrackedSymbolsPort, ProcessMarketDataUseCase processMarketDataUseCase) {
        this.apiKey = apiKey;
        this.loadTrackedSymbolsPort = loadTrackedSymbolsPort;
        this.finnhubWebSocketClient = new FinnhubWebSocketClient(URI.create("wss://ws.finnhub.io?token=" + apiKey),processMarketDataUseCase);
    }


    @PostConstruct
    public void connect() throws InterruptedException {
        finnhubWebSocketClient.connectBlocking();
        loadTrackedSymbolsPort.loadAllSymbols().forEach(this::subscribe);
    }

    @Override
    public void subscribe(String ticker) {
        finnhubWebSocketClient.send(String.format("{\"type\":\"subscribe\",\"symbol\":\"%s\"}", ticker));
    }

    @Override
    public void unsubscribe(String ticker) {
        finnhubWebSocketClient.send(String.format("{\"type\":\"unsubscribe\",\"symbol\":\"%s\"}", ticker));
    }
}
