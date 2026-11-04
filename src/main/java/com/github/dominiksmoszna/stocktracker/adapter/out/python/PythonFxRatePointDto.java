package com.github.dominiksmoszna.stocktracker.adapter.out.python;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PythonFxRatePointDto(
        LocalDate date,
        BigDecimal rate
) {
}
