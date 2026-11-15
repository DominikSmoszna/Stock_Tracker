package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.github.dominiksmoszna.stocktracker.common.exception.InstrumentLookupException;
import com.github.dominiksmoszna.stocktracker.domain.model.InstrumentInfo;
import com.github.dominiksmoszna.stocktracker.domain.port.out.InstrumentInfoPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Currency;
import java.util.Optional;

@Slf4j
@Component
public class PythonInstrumentAdapter implements InstrumentInfoPort {

    private final RestClient restClient;

    public PythonInstrumentAdapter(RestClient pythonRestClient) {
        this.restClient = pythonRestClient;
    }

    @Override
    public Optional<InstrumentInfo> loadInstrument(String symbol) {
        PythonInstrumentDto dto;
        try {
            dto = restClient.get()
                    .uri("/api/instrument/{symbol}", symbol)
                    .retrieve()
                    .body(PythonInstrumentDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            }
            throw new InstrumentLookupException("Instrument lookup failed for " + symbol, e);
        } catch (RestClientException e) {
            throw new InstrumentLookupException("Instrument service unavailable for " + symbol, e);
        }

        if (dto == null || dto.currency() == null) {
            return Optional.empty();
        }

        Currency currency;
        try {
            currency = Currency.getInstance(dto.currency());
        } catch (IllegalArgumentException e) {
            log.warn("Unsupported currency '{}' returned for {}", dto.currency(), symbol);
            return Optional.empty();
        }

        return Optional.of(new InstrumentInfo(
                dto.symbol() != null ? dto.symbol() : symbol,
                dto.name(),
                currency
        ));
    }
}
