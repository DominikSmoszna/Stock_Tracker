package com.github.dominiksmoszna.stocktracker.common.mapper;

import com.github.dominiksmoszna.stocktracker.application.dto.WatchlistDto;
import com.github.dominiksmoszna.stocktracker.domain.model.Watchlist;

public class WatchlistMapper {

    private WatchlistMapper() {}

    public static WatchlistDto toDto(Watchlist watchlist) {
        return new WatchlistDto(
                watchlist.getId(),
                watchlist.getUserId(),
                watchlist.getListName(),
                watchlist.getWatchlist()
        );
    }

}
