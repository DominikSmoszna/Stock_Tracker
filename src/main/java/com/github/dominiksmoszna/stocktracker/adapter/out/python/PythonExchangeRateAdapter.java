package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.github.dominiksmoszna.stocktracker.domain.model.ExchangeRatePoint;
import com.github.dominiksmoszna.stocktracker.domain.port.out.ExchangeRatePort;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

@Component
public class PythonExchangeRateAdapter implements ExchangeRatePort {

    private final RestClient restClient;

    public PythonExchangeRateAdapter(RestClient pythonRestClient) {
        this.restClient = pythonRestClient;
    }

    @Override
    public List<ExchangeRatePoint> getRates(
            Currency from,
            Currency to,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (from.equals(to)) {
            return buildIdentitySeries(startDate, endDate);
        }

        String pair = from.getCurrencyCode() + to.getCurrencyCode();

        PythonFxRatePointDto[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/fx/{pair}")
                        .queryParam("start_date", startDate)
                        .queryParam("end_date", endDate)
                        .build(pair))
                .retrieve()
                .body(PythonFxRatePointDto[].class);

        PythonFxRatePointDto[] safeResponse =
                response != null ? response: new PythonFxRatePointDto[0];

        return Arrays.stream(safeResponse)
                .map(dto -> new ExchangeRatePoint(dto.date(),dto.rate()))
                .toList();
    }

    private List<ExchangeRatePoint> buildIdentitySeries(
            LocalDate start,
            LocalDate end
    ){
        return start.datesUntil(end.plusDays(1))
                .map(date -> new ExchangeRatePoint(date, BigDecimal.ONE))
                .toList();
    }
}
