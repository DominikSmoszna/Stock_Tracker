package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.InstrumentInfo;

import java.util.Optional;

public interface InstrumentInfoPort {

    /**
     * @return instrument metadata, or empty if the symbol is unknown to the market data provider
     */
    Optional<InstrumentInfo> loadInstrument(String symbol);
}
