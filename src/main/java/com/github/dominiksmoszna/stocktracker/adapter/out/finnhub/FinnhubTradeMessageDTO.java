package com.github.dominiksmoszna.stocktracker.adapter.out.finnhub;

import java.util.List;

public record FinnhubTradeMessageDTO(
        String type,
        List<FinnhubTradeDataDTO> data
) {
}
