package com.github.dominiksmoszna.stocktracker.domain.model;

import java.util.Currency;

/**
 * Instrument metadata relevant to the domain.
 *
 * @param currency normalized ISO 4217 trading currency (e.g. GBP, never the minor unit GBp)
 */
public record InstrumentInfo(
        String symbol,
        String name,
        Currency currency
) {
}
