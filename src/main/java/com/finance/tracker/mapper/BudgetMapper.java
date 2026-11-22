package com.finance.tracker.mapper;

import com.finance.tracker.dto.response.BudgetResponse;
import com.finance.tracker.model.Budget;

public final class BudgetMapper {
    private BudgetMapper() { }

    public static BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse(budget.getId(),
                java.time.YearMonth.from(budget.getMonthStart()), budget.getLimitAmount(), budget.getCurrency(),
                budget.getCategory().getId(), budget.getCategory().getName(),
                budget.getCreatedAt(), budget.getUpdatedAt(),
                budget.getCreatedBy(), budget.getLastModifiedBy());
    }
}
