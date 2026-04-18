package com.github.dominiksmoszna.stocktracker.common.mapper;


import com.github.dominiksmoszna.stocktracker.application.dto.AlertDto;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;

public class AlertMapper {

    private AlertMapper() {}

    public static AlertDto toDto(PriceAlert priceAlert) {
        return new AlertDto(
                priceAlert.getId(),
                priceAlert.getUserId(),
                priceAlert.getTicker(),
                priceAlert.getThreshold(),
                priceAlert.getType()
        );
    }

}
