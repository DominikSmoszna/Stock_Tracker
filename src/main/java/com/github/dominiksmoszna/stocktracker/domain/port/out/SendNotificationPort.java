package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;

import java.math.BigDecimal;

public interface SendNotificationPort {
    void sendNotification(PriceAlert priceAlert, BigDecimal currentPrice);
}
