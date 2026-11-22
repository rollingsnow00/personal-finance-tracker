package com.finance.tracker.dto.request;

import com.finance.tracker.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull @Schema(example = "EXPENSE") TransactionType type,
        @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2)
        @Schema(example = "42.50") BigDecimal amount,
        @NotNull @PastOrPresent @Schema(example = "2026-11-20") LocalDate transactionDate,
        @Size(max = 500) @Schema(example = "Weekly groceries") String description,
        @NotNull @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a three-letter ISO code")
        @Schema(example = "PLN") String currency,
        @NotNull @Schema(example = "1") Long categoryId
) { }
