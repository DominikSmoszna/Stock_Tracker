package com.github.dominiksmoszna.stocktracker.adapter.out.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "watchlist")
@NoArgsConstructor
@Getter
public class WatchlistEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "userId", nullable = false)
    private UUID userId;
    @Column(name = "listName", nullable = false)
    private String listName;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tickers", joinColumns = @JoinColumn(name = "watchlist_id"))
    @Column(name = "ticker")
    private List<String> tickers =  new ArrayList<>();

    public WatchlistEntity(UUID id, UUID userId, String listName, List<String> tickers) {
        this.id = id;
        this.userId = userId;
        this.listName = listName;
        this.tickers = tickers != null ? new ArrayList<>(tickers) : new ArrayList<>();
    }

    public void addTicker(String ticker) {
        if(!this.tickers.contains(ticker)) {
            this.tickers.add(ticker);
        }
    }

    public void removeTicker(String ticker) {
        this.tickers.remove(ticker);
    }

}
