package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import java.util.List;

public record PythonPortfolioBackfillResponseDto(
        List<PythonTotalValuePointDto> total,
        List<PythonPositionHistoryDto> positions
) {
}
