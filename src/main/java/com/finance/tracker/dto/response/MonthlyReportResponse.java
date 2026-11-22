package com.finance.tracker.dto.response;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthlyReportResponse(YearMonth month, String currency,
                                    BigDecimal income, BigDecimal expenses, BigDecimal balance,
                                    List<CategorySummary> byCategory) {
    public record CategorySummary(Long categoryId, String categoryName,
                                  BigDecimal spent, BigDecimal budgetLimit, BigDecimal remaining) { }
}
