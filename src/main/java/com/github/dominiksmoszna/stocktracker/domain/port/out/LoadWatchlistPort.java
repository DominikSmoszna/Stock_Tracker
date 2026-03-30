package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.Watchlist;

import java.util.UUID;

public interface LoadWatchlistPort {
    Watchlist loadWatchlist(UUID watchlistId);
}
