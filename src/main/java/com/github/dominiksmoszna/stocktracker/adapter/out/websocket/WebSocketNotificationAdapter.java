package com.github.dominiksmoszna.stocktracker.adapter.out.websocket;

import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import com.github.dominiksmoszna.stocktracker.domain.port.out.SendNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class WebSocketNotificationAdapter implements SendNotificationPort {

    @Override
    public void sendNotification(PriceAlert priceAlert, BigDecimal currentPrice) {
        log.info("Alert triggered - Ticker: {}, Type: {}, Threshold: {}, Current price: {}", priceAlert.getTicker(), priceAlert.getType(), priceAlert.getThreshold(), currentPrice);
    }
}
