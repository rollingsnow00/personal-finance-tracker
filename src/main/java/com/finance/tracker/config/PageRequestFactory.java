package com.finance.tracker.config;

import com.finance.tracker.exception.BusinessRuleException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageRequestFactory {
    private static final int MAX_PAGE_SIZE = 100;

    private PageRequestFactory() { }

    public static Pageable create(int page, int size, String sortBy, String direction, Set<String> allowedFields) {
        if (page < 0) throw new BusinessRuleException("Page must be zero or greater");
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessRuleException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (!allowedFields.contains(sortBy)) {
            throw new BusinessRuleException("Unsupported sort field: " + sortBy);
        }
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException("Sort direction must be asc or desc");
        }
        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }
}
