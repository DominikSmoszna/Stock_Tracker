package com.github.dominiksmoszna.stocktracker.adapter.in.rest;

import com.github.dominiksmoszna.stocktracker.application.dto.AddTickerRequest;
import com.github.dominiksmoszna.stocktracker.application.dto.CreateWatchlistRequest;
import com.github.dominiksmoszna.stocktracker.application.dto.WatchlistDto;
import com.github.dominiksmoszna.stocktracker.common.mapper.WatchlistMapper;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ManageWatchlistUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/watchlists")
@RequiredArgsConstructor
public class WatchlistController {

    private final ManageWatchlistUseCase manageWatchlistUseCase;

    @GetMapping("/{watchlistId}")
    public ResponseEntity<WatchlistDto> getWatchlist(@PathVariable("watchlistId") UUID watchlistId) {
        return  ResponseEntity.ok(WatchlistMapper.toDto(manageWatchlistUseCase.getWatchlist(watchlistId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WatchlistDto>> getUserWatchlist  (@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(manageWatchlistUseCase.getUserWatchlist(userId).stream().map(WatchlistMapper::toDto).toList());
    }

    @PostMapping("/")
    public ResponseEntity<WatchlistDto> addWatchlist(@RequestBody @Valid CreateWatchlistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(WatchlistMapper.toDto(manageWatchlistUseCase.addWatchlist(request.name(), request.userId())));
    }

    @PostMapping("/{watchlistId}/tickers")
    public ResponseEntity<Void> addTicker(@RequestBody @Valid AddTickerRequest request, @PathVariable("watchlistId") UUID watchlistId) {
        manageWatchlistUseCase.addTicker(request.ticker(),  watchlistId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{watchlistId}")
    public ResponseEntity<Void> deleteWatchlist(@PathVariable("watchlistId") UUID watchlistId) {
        manageWatchlistUseCase.removeWatchlist(watchlistId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{watchlistId}/tickers/{ticker}")
    public ResponseEntity<Void> deleteTicker(@PathVariable("ticker") String ticker,@PathVariable("watchlistId") UUID watchlistId) {
        manageWatchlistUseCase.removeTicker(ticker, watchlistId);
        return ResponseEntity.noContent().build();
    }
}
