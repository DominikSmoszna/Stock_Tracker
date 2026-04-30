package com.github.dominiksmoszna.stocktracker.adapter.in.rest;

import com.github.dominiksmoszna.stocktracker.application.dto.MarketQuoteDto;
import com.github.dominiksmoszna.stocktracker.domain.model.HistoricalPrice;
import com.github.dominiksmoszna.stocktracker.domain.model.StockQuote;
import com.github.dominiksmoszna.stocktracker.domain.port.in.GetMarketQuoteUseCase;
import com.github.dominiksmoszna.stocktracker.domain.port.in.GetPriceHistoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

    private final GetMarketQuoteUseCase getMarketQuoteUseCase;
    private final GetPriceHistoryUseCase getPriceHistoryUseCase;

    @GetMapping("/quote/{symbol}")
    public MarketQuoteDto getQuote(@PathVariable("symbol") String symbol) {
        StockQuote quote = getMarketQuoteUseCase.getQuote(symbol);
        return new MarketQuoteDto(
                quote.currentPrice(),
                quote.change(),
                quote.percentChange(),
                quote.high(),
                quote.low(),
                quote.open(),
                quote.previousClose(),
                quote.timestamp(),
                symbol.toUpperCase()
        );
    }

    @GetMapping("/history/{symbol}")
    public List<HistoricalPrice> getHistory(@PathVariable("symbol") String symbol) {
        return getPriceHistoryUseCase.getPriceHistory(symbol);
    }

}
