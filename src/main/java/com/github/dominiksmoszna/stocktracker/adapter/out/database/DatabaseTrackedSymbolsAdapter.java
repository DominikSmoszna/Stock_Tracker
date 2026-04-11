package com.github.dominiksmoszna.stocktracker.adapter.out.database;

import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadTrackedSymbolsPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DatabaseTrackedSymbolsAdapter implements LoadTrackedSymbolsPort {
    @Override
    public List<String> loadAllSymbols() {
        log.info("Loading all symbols");
        return List.of("AAPL", "TSLA");
    }
}
