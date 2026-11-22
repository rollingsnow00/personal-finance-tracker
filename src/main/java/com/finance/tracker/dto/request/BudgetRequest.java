package com.finance.tracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.YearMonth;

public record BudgetRequest(
        @NotNull @Schema(example = "2026-11") YearMonth month,
        @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2)
        @Schema(example = "1500.00") BigDecimal limitAmount,
        @NotNull @Pattern(regexp = "^[A-Z]{3}$") @Schema(example = "PLN") String currency,
        @NotNull @Schema(example = "1") Long categoryId
) { }
