package com.github.dominiksmoszna.stocktracker.adapter.out.yahoo;


import com.github.dominiksmoszna.stocktracker.common.exception.SymbolNotFoundException;
import com.github.dominiksmoszna.stocktracker.domain.model.HistoricalPrice;
import com.github.dominiksmoszna.stocktracker.domain.port.out.GetPriceHistoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class YahooRestClient implements GetPriceHistoryPort {

    private final RestClient restClient;
    private static final String BASE_URL ="https://query1.finance.yahoo.com";

    public YahooRestClient() {
        this.restClient = RestClient.create(BASE_URL);
    }

    @Override
    public List<HistoricalPrice> getPriceHistory(String symbol, String interval, String range) {
        YahooResponseDTO dto = restClient.get()
                .uri("/v8/finance/chart/{symbol}?interval={interval}&range={range}", symbol.toUpperCase(), interval, range)
                .retrieve()
                .body(YahooResponseDTO.class);

        if(dto == null) {
            throw new SymbolNotFoundException("No data for symbol: " + symbol);
        }

        return mapToHistoricalPrices(dto);
    }

    private BigDecimal toDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP) : null;
    }

    private Long toLong(Double value) {
        return value != null ? value.longValue() : 0L;
    }

    private List<HistoricalPrice> mapToHistoricalPrices(YahooResponseDTO dto) {
        YahooResultDTO result = dto.chart().result().getFirst();
        List<Long> timestamps = result.timestamp();
        YahooQuoteDTO quote = result.indicators().quote().getFirst();

        List<HistoricalPrice> prices = new ArrayList<>();

        for (int i = 0; i < timestamps.size(); i++) {
            LocalDate date = Instant.ofEpochSecond(timestamps.get(i)).atZone(ZoneId.of("UTC")).toLocalDate();

            prices.add(new HistoricalPrice(
                    result.meta().symbol().toUpperCase(),
                    date,
                    toDecimal(quote.open().get(i)),
                    toDecimal(quote.high().get(i)),
                    toDecimal(quote.low().get(i)),
                    quote.close().get(i) != null ? toDecimal(quote.close().get(i)) : result.meta().regularMarketPrice(),
                    toLong(quote.volume().get(i))
            ));
        }
        return prices;
    }
}
