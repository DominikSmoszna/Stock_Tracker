package com.github.dominiksmoszna.stocktracker.adapter.out.finnhub;

import com.github.dominiksmoszna.stocktracker.domain.port.in.ProcessMarketDataUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadTrackedSymbolsPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinnhubWebSocketAdapterTest {

    @Mock
    private LoadTrackedSymbolsPort loadTrackedSymbolsPort;

    @Mock
    private ProcessMarketDataUseCase processMarketDataUseCase;

    @Mock
    private FinnhubWebSocketClient mockClient;

    @Test
    void shouldConnectNonBlockingWhenEnabledAndApiKeyPresent() {
        FinnhubWebSocketAdapter adapter = new FinnhubWebSocketAdapter(
                "test-key", true, loadTrackedSymbolsPort, mockClient
        );

        adapter.connect();

        verify(mockClient).connect();
        verify(mockClient, never()). send(anyString());
    }

    @Test
    void shouldSkipConnectWhenWebsocketDisabled() {
        FinnhubWebSocketAdapter adapter = new FinnhubWebSocketAdapter(
                "test-key", false, loadTrackedSymbolsPort, mockClient
        );

        adapter.connect();

        verifyNoInteractions(mockClient);
    }

    @Test
    void shouldSkipConnectWhenApiKeyBlank() {
        FinnhubWebSocketAdapter adapter = new FinnhubWebSocketAdapter(
                "   ", true, loadTrackedSymbolsPort, mockClient
        );

        adapter.connect();

        verifyNoInteractions(mockClient);
    }

    @Test
    void shouldNotThrowWhenSubscribingOrUnsubscribingWhileSocketClosed() {
        when(mockClient.isOpen()).thenReturn(false);
        FinnhubWebSocketAdapter adapter = new FinnhubWebSocketAdapter(
                "test-key", true, loadTrackedSymbolsPort, mockClient
        );

        assertThatCode(() -> {
            adapter.subscribe("AAPL");
            adapter.unsubscribe("AAPL");
        }).doesNotThrowAnyException();

        verify(mockClient, never()).send(anyString());
    }

    @Test
    void shouldSendSubscribeForAllTrackedSymbolsWhenSocketOpen() {
        when(mockClient.isOpen()).thenReturn(true);
        when(loadTrackedSymbolsPort.loadAllSymbols()).thenReturn(List.of("AAPL", "MSFT"));
        FinnhubWebSocketAdapter adapter = new FinnhubWebSocketAdapter(
                "test-key", true, loadTrackedSymbolsPort, mockClient
        );

        adapter.subscribeAllTrackedSymbols();

        verify(mockClient).send("{\"type\":\"subscribe\",\"symbol\":\"AAPL\"}");
        verify(mockClient).send("{\"type\":\"subscribe\",\"symbol\":\"MSFT\"}");
    }

    @Test
    void shouldInvokeOnConnectedCallbackOnEveryOpenIncludingReconnect() {
        AtomicInteger callbackInvocations = new AtomicInteger(0);
        FinnhubWebSocketClient client = new FinnhubWebSocketClient(
                URI.create("wss://ws.finnhub.io?token=test"),
                processMarketDataUseCase,
                callbackInvocations::incrementAndGet
        );

        client.onOpen(null);
        client.onOpen(null);

        assertThat(callbackInvocations.get()).isEqualTo(2);
    }
}
