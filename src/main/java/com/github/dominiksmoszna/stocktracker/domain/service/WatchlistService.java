package com.github.dominiksmoszna.stocktracker.domain.service;

import com.github.dominiksmoszna.stocktracker.common.exception.WatchlistNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.Watchlist;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ManageWatchlistUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadWatchlistPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ManageWatchlistPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.StockSubscriptionPort;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class WatchlistService implements ManageWatchlistUseCase {

    private final LoadWatchlistPort loadWatchlistPort;
    private final ManageWatchlistPort manageWatchlistPort;
    private final StockSubscriptionPort stockSubscriptionPort;


    @Override
    public void addTicker(String ticker, UUID watchlistId) {
        Optional.ofNullable(loadWatchlistPort.loadWatchlist(watchlistId))
                .ifPresentOrElse(watchlist -> {manageWatchlistPort.addTickerToWatchlist(watchlistId, ticker);
                                                        stockSubscriptionPort.subscribe(ticker);},
                        () -> {throw new WatchlistNotFoundException("Watchlist not found");});
    }

    @Override
    public void removeTicker(String ticker, UUID watchlistId) {
        Optional.ofNullable(loadWatchlistPort.loadWatchlist(watchlistId))
                .ifPresentOrElse(watchlist -> {manageWatchlistPort.removeTickerFromWatchlist(watchlistId, ticker);
                                                        if (!loadWatchlistPort.checkWatchlistsForTicker(ticker)) {stockSubscriptionPort.unsubscribe(ticker);};},
                        () -> {throw new WatchlistNotFoundException("Watchlist not found");});
    }

    @Override
    public Watchlist addWatchlist(String name, UUID userId) {
        UUID id = UUID.randomUUID();
        Watchlist watchlist = new Watchlist(id, userId, name, List.of());
        manageWatchlistPort.saveWatchlist(watchlist);
        return watchlist;
    }

    @Override
    public void removeWatchlist(UUID watchlistId) {
        Optional.ofNullable(loadWatchlistPort.loadWatchlist(watchlistId))
                .ifPresentOrElse(watchlist -> {manageWatchlistPort.deleteWatchlist(watchlistId);},
                        () -> {throw new WatchlistNotFoundException("Watchlist not found");});
    }

    @Override
    public List<Watchlist> getAllWatchlists(UUID userId) {
        return loadWatchlistPort.loadAllWatchlists(userId);
    }

    @Override
    public Watchlist getWatchlist(UUID watchlistId) {
        return Optional.ofNullable(loadWatchlistPort.loadWatchlist(watchlistId))
                .orElseThrow(()-> new WatchlistNotFoundException("Watchlist not found"));
    }
}
