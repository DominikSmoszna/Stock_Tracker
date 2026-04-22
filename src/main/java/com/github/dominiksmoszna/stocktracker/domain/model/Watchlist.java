package com.github.dominiksmoszna.stocktracker.domain.model;

import java.util.List;
import java.util.UUID;

public record Watchlist(UUID id, UUID userId, String listName, List<String> watchlist) {
}
