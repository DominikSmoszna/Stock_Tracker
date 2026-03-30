package com.github.dominiksmoszna.stocktracker.domain.port.out;

import com.github.dominiksmoszna.stocktracker.domain.model.Watchlist;

import java.util.UUID;

public interface ManageWatchlistPort {
    Watchlist saveWatchlist(Watchlist watchlist);
    Watchlist addTickerToWatchlist(UUID watchlistId, String ticker);
    Watchlist removeTickerFromWatchlist(UUID watchlistId, String ticker);
    void deleteWatchlist(UUID watchlistId);
}
