package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import com.github.dominiksmoszna.stocktracker.common.exception.PortfolioValuationException;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioPosition;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioValuationResult;
import com.github.dominiksmoszna.stocktracker.domain.model.PortfolioValuePoint;
import com.github.dominiksmoszna.stocktracker.domain.model.PositionValuationHistory;
import com.github.dominiksmoszna.stocktracker.domain.model.PositionValuePoint;
import com.github.dominiksmoszna.stocktracker.domain.port.out.PortfolioValuationPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

@Component
public class PythonPortfolioValuationAdapter implements PortfolioValuationPort {

    private static final String DEFAULT_ERROR_MESSAGE = "Failed to fetch historical market data";

    private final RestClient restClient;

    public PythonPortfolioValuationAdapter(RestClient pythonRestClient) {
        this.restClient = pythonRestClient;
    }

    @Override
    public PortfolioValuationResult getHistoricalValuation(
            List<PortfolioPosition> positions,
            Currency targetCurrency,
            LocalDate endDate
    ) throws PortfolioValuationException {
        if (positions == null || positions.isEmpty()) {
            return new PortfolioValuationResult(List.of(), List.of());
        }

        List<PythonPositionInputDto> positionInputs = positions.stream()
                .map(pos -> new PythonPositionInputDto(
                        pos.id(),
                        pos.ticker(),
                        pos.purchaseDate(),
                        pos.quantity(),
                        pos.currency().getCurrencyCode()
                ))
                .toList();

        PythonPortfolioBackfillRequestDto requestDto = new PythonPortfolioBackfillRequestDto(
                positionInputs,
                targetCurrency.getCurrencyCode(),
                endDate
        );

        PythonPortfolioBackfillResponseDto response;
        try {
            response = restClient.post()
                    .uri("/api/portfolio/backfill")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestDto)
                    .retrieve()
                    .body(PythonPortfolioBackfillResponseDto.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.BAD_GATEWAY)) {
                throw toPortfolioValuationException(e);
            }
            throw new PortfolioValuationException(
                    "Unexpected response from valuation service: " + e.getStatusCode(),
                    List.of(),
                    List.of()
            );
        } catch (RestClientException e) {
            throw new PortfolioValuationException(
                    "Valuation service unavailable",
                    List.of(),
                    List.of()
            );
        }

        if (response == null) {
            return new PortfolioValuationResult(List.of(), List.of());
        }

        List<PortfolioValuePoint> total = response.total() == null
                ? List.of()
                : response.total().stream()
                        .map(dto -> new PortfolioValuePoint(dto.date(), dto.totalValue()))
                        .toList();

        List<PositionValuationHistory> positionHistories = response.positions() == null
                ? List.of()
                : response.positions().stream()
                        .map(posDto -> new PositionValuationHistory(
                                posDto.positionId(),
                                posDto.symbol(),
                                posDto.values() == null
                                        ? List.of()
                                        : posDto.values().stream()
                                                .map(v -> new PositionValuePoint(v.date(), v.value()))
                                                .toList()
                        ))
                        .toList();

        return new PortfolioValuationResult(total, positionHistories);
    }

    private PortfolioValuationException toPortfolioValuationException(RestClientResponseException e) {
        try {
            PythonErrorResponseDto errorDto = e.getResponseBodyAs(PythonErrorResponseDto.class);
            if (errorDto != null && errorDto.detail() != null) {
                PythonErrorResponseDto.Detail detail = errorDto.detail();
                String message = detail.message() != null ? detail.message() : DEFAULT_ERROR_MESSAGE;
                List<String> priceSymbols = detail.priceSymbols() != null ? detail.priceSymbols() : List.of();
                List<String> fxSymbols = detail.fxSymbols() != null ? detail.fxSymbols() : List.of();
                return new PortfolioValuationException(message, priceSymbols, fxSymbols);
            }
        } catch (Exception ignored) {
            // Fall through to default exception when error body is not valid JSON
        }
        return new PortfolioValuationException(DEFAULT_ERROR_MESSAGE, List.of(), List.of());
    }
}
