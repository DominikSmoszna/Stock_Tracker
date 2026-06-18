package com.github.dominiksmoszna.stocktracker.infrastructure.config;

import com.github.dominiksmoszna.stocktracker.domain.port.out.*;
import com.github.dominiksmoszna.stocktracker.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public WatchlistService watchlistService(
            LoadWatchlistPort loadWatchlistPort,
            ManageWatchlistPort manageWatchlistPort,
            StockSubscriptionPort stockSubscriptionPort
            ) {
        return new WatchlistService(
                loadWatchlistPort,
                manageWatchlistPort,
                stockSubscriptionPort
        );
    }

    @Bean
    public MarketDataService marketDataService(
            CurrentPricePort currentPricePort,
            SaveCurrentPricePort saveCurrentPricePort,
            PriceHistoryPort priceHistoryPort,
            PriceUpdatePort priceUpdatePort,
            SendNotificationPort sendNotificationPort,
            LoadAlertPort loadAlertPort
    ) {
        return new MarketDataService(
                currentPricePort,
                saveCurrentPricePort,
                priceHistoryPort,
                priceUpdatePort,
                sendNotificationPort,
                loadAlertPort
        );
    }

    @Bean
    public AlertService alertService(
            LoadAlertPort loadAlertPort,
            ManageAlertPort manageAlertPort
    ) {
        return new AlertService(
                loadAlertPort,
                manageAlertPort
        );
    }

    @Bean
    public MarketQuoteService marketQuoteService(
            MarketDataPort marketDataPort
    ){
        return new MarketQuoteService(
                marketDataPort
        );
    }
}
