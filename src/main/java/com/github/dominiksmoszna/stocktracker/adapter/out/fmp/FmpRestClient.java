package com.github.dominiksmoszna.stocktracker.adapter.out.fmp;

import com.github.dominiksmoszna.stocktracker.common.exception.SymbolNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.HistoricalPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.out.GetPriceHistoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class FmpRestClient implements GetPriceHistoryPort {

    private final RestClient restClient;
    private final String apiKey;
    private static final String BASE_URL = "https://financialmodelingprep.com/stable";

    public FmpRestClient(@Value("${fmp.api.key}") String apiKey){
        this.apiKey = apiKey;
        this.restClient = RestClient.create(BASE_URL);
    }

    @Override
    public List<HistoricalPrice> getPriceHistory(String symbol){
        log.info("FMP API Key: {}", apiKey);
        FmpHistoricalPriceDto[] dtos = restClient.get()
                .uri("/historical-price-eod/full?symbol={symbol}&apikey={apiKey}",symbol.toUpperCase(),apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .body(FmpHistoricalPriceDto[].class);
        if (dtos == null || dtos.length == 0) {
            throw new SymbolNotFoundException("No historical data for symbol: " + symbol);
        }

        return Arrays.stream(dtos).map(h -> new HistoricalPrice(
                symbol.toUpperCase(),
                h.date(),
                h.open(),
                h.high(),
                h.low(),
                h.close(),
                h.volume(),
                h.change(),
                h.changePercent(),
                h.vwap()
        )).toList();
    }

}
