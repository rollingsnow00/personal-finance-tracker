package com.finance.tracker.dto.response;

import com.finance.tracker.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionResponse(Long id, TransactionType type, BigDecimal amount,
                                  LocalDate transactionDate, String description, String currency,
                                  Long categoryId, String categoryName,
                                  Instant createdAt, Instant updatedAt,
                                  String createdBy, String lastModifiedBy) { }
