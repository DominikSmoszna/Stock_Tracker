package com.github.dominiksmoszna.stocktracker.adapter.out.database;

import com.github.dominiksmoszna.stocktracker.domain.model.Watchlist;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadWatchlistPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ManageWatchlistPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class DatabaseWatchlistAdapter implements LoadWatchlistPort, ManageWatchlistPort {
    @Override
    public Watchlist loadWatchlist(UUID watchlistId) {
        log.info("Loading watchlist: {}", watchlistId);
        return null;
    }

    @Override
    public List<Watchlist> loadAllWatchlists(UUID userId) {
        log.info("Loading all watchlists for user: {}", userId);
        return List.of();
    }

    @Override
    public boolean checkWatchlistsForTicker(String ticker) {
        log.info("Checking watchlists for ticker: {}", ticker);
        return false;
    }

    @Override
    public void saveWatchlist(Watchlist watchlist) {
        log.info("Saving watchlist - Watchlist Id: {}, Watchlist Title: {}", watchlist.getId(), watchlist.getListName());
    }

    @Override
    public void addTickerToWatchlist(UUID watchlistId, String ticker) {
        log.info("Adding ticker: {} to watchlist: {}", ticker, watchlistId);
    }

    @Override
    public void removeTickerFromWatchlist(UUID watchlistId, String ticker) {
        log.info("Removing ticker: {} from watchlist: {}", ticker, watchlistId);
    }

    @Override
    public void deleteWatchlist(UUID watchlistId) {
        log.info("Deleting watchlist: {}", watchlistId);

    }
}
