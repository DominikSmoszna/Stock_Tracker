package com.github.dominiksmoszna.stocktracker.adapter.out.finnhub;

import com.github.dominiksmoszna.stocktracker.common.exception.SymbolNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.StockQuote;
import com.github.dominiksmoszna.stocktracker.domain.port.out.MarketDataPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class FinnhubRestClient implements MarketDataPort {

    private final RestClient restClient;
    private final String apiKey;
    private static final String BASE_URL ="https://finnhub.io/api/v1";

    public FinnhubRestClient(@Value("${finnhub.api.key}") String apiKey) {
        this.restClient = RestClient.create(BASE_URL);
        this.apiKey = apiKey;
    }

    public StockQuote getQuote(String symbol) {
        FinnhubQuoteDTO dto = restClient.get()
                .uri("/quote?symbol={symbol}&token={token}", symbol.toUpperCase(), apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .body(FinnhubQuoteDTO.class);

        if(dto == null) {
            throw new SymbolNotFoundException("No quote data for symbol: " + symbol);
        }

        return new StockQuote(
                dto.currentPrice(),
                dto.change(),
                dto.percentChange(),
                dto.high(),
                dto.low(),
                dto.open(),
                dto.previousClose(),
                dto.timestamp(),
                symbol.toUpperCase()
        );
    }
}
