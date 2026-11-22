package com.finance.tracker.dto.request;

import com.finance.tracker.model.TransactionType;
import com.finance.tracker.validation.ValidDateRange;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@ValidDateRange
public record TransactionSearchRequest(
        LocalDate from,
        LocalDate to,
        TransactionType type,
        Long categoryId,
        @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal minAmount,
        @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal maxAmount,
        @Size(max = 120) String query
) { }
