package com.github.dominiksmoszna.stocktracker.adapter.out.database;

import com.github.dominiksmoszna.stocktracker.adapter.out.database.entity.WatchlistEntity;
import com.github.dominiksmoszna.stocktracker.adapter.out.database.repository.WatchlistJpaRepository;
import com.github.dominiksmoszna.stocktracker.common.exception.WatchlistNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.Watchlist;
import com.github.dominiksmoszna.stocktracker.domain.port.out.LoadWatchlistPort;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ManageWatchlistPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseWatchlistAdapter implements LoadWatchlistPort, ManageWatchlistPort {

    private final WatchlistJpaRepository repository;

    @Override
    public Watchlist loadWatchlist(UUID watchlistId) {
        log.info("Loading watchlist: {}", watchlistId);
        return repository.findById(watchlistId).map(this::toDomain).orElse(null);
    }

    @Override
    public List<Watchlist> loadUserWatchlist(UUID userId) {
        log.info("Loading all watchlists for user: {}", userId);
        return repository.findAllByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean checkWatchlistForTicker(String ticker) {
        log.info("Checking watchlists for ticker: {}", ticker);
        return repository.existsByTicker(ticker);
    }

    @Override
    public void saveWatchlist(Watchlist watchlist) {
        WatchlistEntity entity = toEntity(watchlist);
        repository.save(entity);
        log.info("Saving watchlist - Watchlist Id: {}, Watchlist Title: {}", watchlist.getId(), watchlist.getListName());
    }

    @Override
    public void addTickerToWatchlist(UUID watchlistId, String ticker) {
        WatchlistEntity entity = repository.findById(watchlistId)
                        .orElseThrow(() -> new WatchlistNotFoundException("watchlist with id: " + watchlistId + " not found"));
        entity.addTicker(ticker);
        repository.save(entity);
        log.info("Adding ticker: {} to watchlist: {}", ticker, watchlistId);
    }

    @Override
    public void removeTickerFromWatchlist(UUID watchlistId, String ticker) {
        WatchlistEntity entity = repository.findById(watchlistId)
                        .orElseThrow(() ->  new WatchlistNotFoundException("watchlist with id: " + watchlistId + " not found"));
        entity.removeTicker(ticker);
        repository.save(entity);
        log.info("Removing ticker: {} from watchlist: {}", ticker, watchlistId);
    }

    @Override
    public void deleteWatchlist(UUID watchlistId) {
        if (!repository.existsById(watchlistId)) {
            throw new  WatchlistNotFoundException("watchlist with id: " + watchlistId + " not found");
        }
        repository.deleteById(watchlistId);
        log.info("Deleting watchlist: {}", watchlistId);
    }

    private WatchlistEntity toEntity(Watchlist watchlist) {
        return new WatchlistEntity(
                watchlist.getId(),
                watchlist.getUserId(),
                watchlist.getListName(),
                watchlist.getWatchlist()
        );
    }

    private Watchlist toDomain(WatchlistEntity watchlist) {
        return new Watchlist(
                watchlist.getId(),
                watchlist.getUserId(),
                watchlist.getListName(),
                new ArrayList<>(watchlist.getTickers())
        );
    }
}
