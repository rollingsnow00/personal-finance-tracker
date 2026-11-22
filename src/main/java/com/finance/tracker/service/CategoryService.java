package com.finance.tracker.service;

import com.finance.tracker.dto.request.CategoryRequest;
import com.finance.tracker.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;

public interface CategoryService {
    Page<CategoryResponse> list(String username, int page, int size, String sortBy, String direction);
    CategoryResponse get(String username, Long id);
    CategoryResponse create(String username, CategoryRequest request);
    CategoryResponse update(String username, Long id, CategoryRequest request);
    void delete(String username, Long id);
    CategoryResponse restore(Long id);
}
