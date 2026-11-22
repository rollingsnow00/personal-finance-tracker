package com.finance.tracker.mapper;

import com.finance.tracker.dto.response.TransactionResponse;
import com.finance.tracker.model.FinanceTransaction;

public final class TransactionMapper {
    private TransactionMapper() { }

    public static TransactionResponse toResponse(FinanceTransaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getType(), transaction.getAmount(),
                transaction.getTransactionDate(), transaction.getDescription(), transaction.getCurrency(),
                transaction.getCategory().getId(), transaction.getCategory().getName(),
                transaction.getCreatedAt(), transaction.getUpdatedAt(),
                transaction.getCreatedBy(), transaction.getLastModifiedBy());
    }
}
