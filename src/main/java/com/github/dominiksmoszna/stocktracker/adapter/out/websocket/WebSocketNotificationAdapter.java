package com.github.dominiksmoszna.stocktracker.adapter.out.websocket;

import com.github.dominiksmoszna.stocktracker.common.mapper.AlertMapper;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.port.out.SendNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotificationAdapter implements SendNotificationPort {

    // Broadcast topic; payload contains userId so the client can filter until per-user destinations (auth) exist.
    static final String DESTINATION = "/topic/alerts";

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void sendNotification(PriceAlert priceAlert, BigDecimal currentPrice) {
        log.info("Alert triggered - Ticker: {}, Type: {}, Threshold: {}, Current price: {}", priceAlert.ticker(), priceAlert.type(), priceAlert.threshold(), currentPrice);
        try {
            simpMessagingTemplate.convertAndSend(DESTINATION, AlertMapper.toNotificationDto(priceAlert, currentPrice));
        } catch (Exception e) {
            log.error("Error while sending alert notification for alert {}: {}", priceAlert.id(), e.getMessage());
        }
    }
}
