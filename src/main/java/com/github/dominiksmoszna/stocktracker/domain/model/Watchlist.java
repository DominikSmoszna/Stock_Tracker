package com.github.dominiksmoszna.stocktracker.domain.model;

import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class Watchlist {
    UUID id;
    UUID userId;
    String listName;
    List<String> watchlist;
}
