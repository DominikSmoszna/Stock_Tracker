package com.github.dominiksmoszna.stocktracker.adapter.out.websocket;

import com.github.dominiksmoszna.stocktracker.domain.model.StockPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.out.PriceUpdatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class WebSocketPricePublisher implements PriceUpdatePort{

    private final SimpMessagingTemplate simpMessagingTemplate;
    private static final String DESTINATION = "/topic/prices";

    @Override
    public void updateCurrentPrice(StockPrice stockPrice) {
        try {
            simpMessagingTemplate.convertAndSend(DESTINATION, stockPrice);
        }catch (Exception e){
            log.error("Error while sending price update: {}", e.getMessage());
        }
    }
}
