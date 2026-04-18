package com.github.dominiksmoszna.stocktracker.application.dto;

import java.util.List;
import java.util.UUID;

public record WatchlistDto(
        UUID id,
        UUID userId,
        String listName,
        List<String> watchlist
) {
}
