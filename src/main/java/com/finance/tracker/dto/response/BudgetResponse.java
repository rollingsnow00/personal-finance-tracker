package com.finance.tracker.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

public record BudgetResponse(Long id, YearMonth month, BigDecimal limitAmount, String currency,
                             Long categoryId, String categoryName,
                             Instant createdAt, Instant updatedAt,
                             String createdBy, String lastModifiedBy) { }
