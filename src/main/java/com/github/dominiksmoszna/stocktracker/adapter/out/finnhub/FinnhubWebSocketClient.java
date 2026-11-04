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

    private static final long RECONNECT_DELAY_MS = 5_000L;

    private final ProcessMarketDataUseCase processMarketDataUseCase;
    private final Runnable onConnectedCallback;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean reconnectEnabled = true;

    public FinnhubWebSocketClient(URI serverUri, ProcessMarketDataUseCase processMarketDataUseCase) {
        this(serverUri, processMarketDataUseCase, () -> {});
    }

    public FinnhubWebSocketClient(
            URI serverUri,
            ProcessMarketDataUseCase processMarketDataUseCase,
            Runnable onConnectedCallback
    ) {
        super(serverUri);
        this.processMarketDataUseCase = processMarketDataUseCase;
        this.onConnectedCallback = onConnectedCallback != null ? onConnectedCallback : () -> {};
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("Websocket connection established");
        try {
            onConnectedCallback.run();
        } catch (Exception e) {
            log.error("Error while subscribing tracked symbols after WebSocket open", e);
        }
    }

    @Override
    public void onMessage(String s) {
        try {
            FinnhubTradeMessageDTO tradeMessage = objectMapper.readValue(s, FinnhubTradeMessageDTO.class);
            if ("trade".equals(tradeMessage.type()) && tradeMessage.data() != null) {
                List<StockPrice> stockPrices = tradeMessage.data().stream().map(data -> new StockPrice(
                        data.symbol(),
                        data.price(),
                        Instant.ofEpochMilli(data.timestamp())
                )).toList();
                stockPrices.forEach(processMarketDataUseCase::processPrice);
            } else {
                log.info("Different type message {}", tradeMessage.type());
            }
        } catch (JacksonException e) {
            log.error("Error parsing finnhub trade message.", e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (reconnectEnabled && remote) {
            log.info("Websocket connection closed by remote (code: {}, reason: {}), scheduling reconnect", code, reason);
            Thread reconnectThread = new Thread(() -> {
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                    if (!reconnectEnabled) {
                        return;
                    }
                    log.info("Trying to reconnect to Finnhub");
                    reconnectBlocking();
                } catch (InterruptedException e) {
                    log.error("Reconnection interrupted", e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("Error while trying to reconnect to Finnhub", e);
                }
            }, "finnhub-ws-reconnect");
            reconnectThread.setDaemon(true);
            reconnectThread.start();
        } else {
            log.info("Websocket connection closed (code: {}, reason: {}, remote: {})", code, reason, remote);
        }
    }

    @Override
    public void onError(Exception e) {
        log.error(e.getMessage(), e);
    }

    public void shutdown() {
        reconnectEnabled = false;
        close();
    }
}
