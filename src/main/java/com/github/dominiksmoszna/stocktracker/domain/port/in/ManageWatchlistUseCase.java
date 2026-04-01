package com.github.dominiksmoszna.stocktracker.domain.port.in;

import com.github.dominiksmoszna.stocktracker.domain.model.Watchlist;

import java.util.List;
import java.util.UUID;

public interface ManageWatchlistUseCase {

    void addTicker(String ticker, UUID watchlistId);
    void removeTicker(String ticker, UUID watchlistId);
    Watchlist addWatchlist(String name, UUID userId);
    void removeWatchlist(UUID watchlistId);
    List<Watchlist> getAllWatchlists(UUID userId);
    Watchlist getWatchlist(UUID watchlistId);
}
