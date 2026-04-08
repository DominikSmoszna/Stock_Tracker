package com.github.dominiksmoszna.stocktracker.domain.port.out;


public interface StockSubscriptionPort {

    void subscribe(String ticker);

    void unsubscribe(String ticker);
}
