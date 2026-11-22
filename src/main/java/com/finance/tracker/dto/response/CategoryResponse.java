package com.finance.tracker.dto.response;

import com.finance.tracker.model.CategoryType;

import java.time.Instant;

public record CategoryResponse(Long id, String name, CategoryType type,
                               Instant createdAt, Instant updatedAt,
                               String createdBy, String lastModifiedBy) { }
