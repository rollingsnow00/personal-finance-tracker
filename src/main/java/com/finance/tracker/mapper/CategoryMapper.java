package com.finance.tracker.mapper;

import com.finance.tracker.dto.response.CategoryResponse;
import com.finance.tracker.model.Category;

public final class CategoryMapper {
    private CategoryMapper() { }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType(),
                category.getCreatedAt(), category.getUpdatedAt(),
                category.getCreatedBy(), category.getLastModifiedBy());
    }
}
