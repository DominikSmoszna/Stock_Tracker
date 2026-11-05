package com.github.dominiksmoszna.stocktracker.common.mapper;


import com.github.dominiksmoszna.stocktracker.application.dto.AlertDto;
import com.github.dominiksmoszna.stocktracker.application.dto.AlertNotificationDto;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;

import java.math.BigDecimal;

public class AlertMapper {

    private AlertMapper() {}

    public static AlertDto toDto(PriceAlert priceAlert) {
        return new AlertDto(
                priceAlert.id(),
                priceAlert.userId(),
                priceAlert.ticker(),
                priceAlert.threshold(),
                priceAlert.type(),
                priceAlert.triggered(),
                priceAlert.triggeredAt()
        );
    }

    public static AlertNotificationDto toNotificationDto(PriceAlert priceAlert, BigDecimal currentPrice) {
        return new AlertNotificationDto(
                priceAlert.id(),
                priceAlert.userId(),
                priceAlert.ticker(),
                priceAlert.type(),
                priceAlert.threshold(),
                currentPrice,
                priceAlert.triggeredAt()
        );
    }

}
