package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.Watchlist;

import java.util.UUID;

public interface ManageWatchlistPort {
    void saveWatchlist(Watchlist watchlist);
    void addTickerToWatchlist(UUID watchlistId, String ticker);
    void removeTickerFromWatchlist(UUID watchlistId, String ticker);
    void deleteWatchlist(UUID watchlistId);
}
