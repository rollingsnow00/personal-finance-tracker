package com.finance.tracker.service;

import com.finance.tracker.dto.request.BudgetRequest;
import com.finance.tracker.dto.response.BudgetResponse;
import org.springframework.data.domain.Page;

public interface BudgetService {
    Page<BudgetResponse> list(String username, int page, int size, String sortBy, String direction);
    BudgetResponse get(String username, Long id);
    BudgetResponse create(String username, BudgetRequest request);
    BudgetResponse update(String username, Long id, BudgetRequest request);
    void delete(String username, Long id);
    BudgetResponse restore(Long id);
}
