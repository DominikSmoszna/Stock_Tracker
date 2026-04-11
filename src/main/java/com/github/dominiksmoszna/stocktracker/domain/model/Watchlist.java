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

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getListName() {
        return listName;
    }

    public List<String> getWatchlist() {
        return watchlist;
    }
}
