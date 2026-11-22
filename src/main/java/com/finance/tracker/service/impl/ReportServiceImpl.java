package com.finance.tracker.service.impl;

import com.finance.tracker.dto.response.MonthlyReportResponse;
import com.finance.tracker.model.Budget;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.FinanceTransactionRepository;
import com.finance.tracker.service.CurrentUserService;
import com.finance.tracker.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private final FinanceTransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final CurrentUserService currentUserService;

    public ReportServiceImpl(FinanceTransactionRepository transactionRepository,
                             BudgetRepository budgetRepository, CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyReportResponse monthly(String username, YearMonth month, String currency) {
        User user = currentUserService.require(username);
        String normalizedCurrency = currency.toUpperCase(Locale.ROOT);
        LocalDate from = month.atDay(1);
        LocalDate toExclusive = month.plusMonths(1).atDay(1);
        BigDecimal income = transactionRepository.sumByUserAndTypeAndDateRange(
                user.getId(), TransactionType.INCOME, from, toExclusive, normalizedCurrency);
        BigDecimal expenses = transactionRepository.sumByUserAndTypeAndDateRange(
                user.getId(), TransactionType.EXPENSE, from, toExclusive, normalizedCurrency);

        Map<Long, CategoryTotals> byCategory = new LinkedHashMap<>();
        List<Object[]> totals = transactionRepository.sumByCategoryAndDateRange(
                user.getId(), TransactionType.EXPENSE, from, toExclusive, normalizedCurrency);
        for (Object[] row : totals) {
            Long categoryId = (Long) row[0];
            String categoryName = (String) row[1];
            BigDecimal spent = (BigDecimal) row[2];
            byCategory.put(categoryId, new CategoryTotals(categoryName, spent, ZERO));
        }
        for (Budget budget : budgetRepository.findAllByUser_IdAndMonthStartAndDeletedAtIsNull(user.getId(), from)) {
            if (!budget.getCurrency().equals(normalizedCurrency)) continue;
            byCategory.compute(budget.getCategory().getId(), (id, current) ->
                    new CategoryTotals(budget.getCategory().getName(), current == null ? ZERO : current.spent(), budget.getLimitAmount()));
        }

        List<MonthlyReportResponse.CategorySummary> summaries = byCategory.entrySet().stream()
                .map(entry -> new MonthlyReportResponse.CategorySummary(entry.getKey(), entry.getValue().name(),
                        entry.getValue().spent(), entry.getValue().budget(), entry.getValue().budget().subtract(entry.getValue().spent())))
                .toList();
        return new MonthlyReportResponse(month, normalizedCurrency, income, expenses, income.subtract(expenses), summaries);
    }

    private record CategoryTotals(String name, BigDecimal spent, BigDecimal budget) { }
}
