package com.github.dominiksmoszna.stocktracker.adapter.out.finnhub;

import com.github.dominiksmoszna.stocktracker.domain.model.StockPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ProcessMarketDataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Slf4j
public class FinnhubWebSocketClient extends WebSocketClient {

    private final ProcessMarketDataUseCase processMarketDataUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FinnhubWebSocketClient(URI serverUri, ProcessMarketDataUseCase processMarketDataUseCase) {
        super(serverUri);
        this.processMarketDataUseCase = processMarketDataUseCase;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("Websocket connection established");
    }

    @Override
    public void onMessage(String s) {
        try {
            FinnhubTradeMessageDTO tradeMessage = objectMapper.readValue(s, FinnhubTradeMessageDTO.class);
            if (tradeMessage.type().equals("trade")) {
                List<StockPrice> stockPrices = tradeMessage.data().stream().map(data -> new StockPrice(
                        data.symbol(),
                        data.price(),
                        Instant.ofEpochMilli(data.timestamp())
                )).toList();
                stockPrices.forEach(processMarketDataUseCase::processPrice);
            }else{
                log.info("Different type message {}", tradeMessage.type());
            }
        } catch (JacksonException e) {
            log.error("Error parsing finnhub trade message.", e);
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        if (!b){
            log.info("Websocket connection closed unexpectedly, exit code: {}", i);
            new Thread(() -> {
                try {
                    log.info("Trying to reconnect to Finnhub");
                    reconnectBlocking();
                } catch (InterruptedException e) {
                        log.error("Reconnection interrupted", e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("Error while trying to reconnect to Finnhub", e);
                }
            }).start();
        }
        else {
            log.info("Websocket connection closed, exit code: {}", i);
        }
    }

    @Override
    public void onError(Exception e) {
        log.error(e.getMessage(), e);
    }
}
