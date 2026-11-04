package com.github.dominiksmoszna.stocktracker;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full Spring context smoke test.
 * Requires running PostgreSQL and a valid FINNHUB_API_KEY (Finnhub WebSocket connects on startup),
 * hence tagged as integration and excluded from the default {@code mvn test} run.
 */
@Tag("integration")
@SpringBootTest
class StockTrackerApplicationTests {

    @Test
    void contextLoads() {
    }

}
