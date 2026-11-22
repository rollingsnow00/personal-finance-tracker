package com.finance.tracker.dto.request;

import com.finance.tracker.model.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 80) @Schema(example = "Groceries") String name,
        @NotNull @Schema(example = "EXPENSE") CategoryType type
) { }
