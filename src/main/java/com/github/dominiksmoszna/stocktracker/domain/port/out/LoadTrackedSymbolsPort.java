package com.github.dominiksmoszna.stocktracker.domain.port.out;

import java.util.List;

public interface LoadTrackedSymbolsPort {
    List<String> loadAllSymbols();
}
