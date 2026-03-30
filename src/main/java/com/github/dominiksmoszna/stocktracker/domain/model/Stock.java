package com.github.dominiksmoszna.stocktracker.domain.model;

import lombok.Value;

@Value
public class Stock {
    String ticker;
    String name;
}
