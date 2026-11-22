package com.finance.tracker.service;

import com.finance.tracker.dto.request.TransactionRequest;
import com.finance.tracker.dto.request.TransactionSearchRequest;
import com.finance.tracker.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;

public interface TransactionService {
    Page<TransactionResponse> search(String username, TransactionSearchRequest filters,
                                     int page, int size, String sortBy, String direction);
    TransactionResponse get(String username, Long id);
    TransactionResponse create(String username, TransactionRequest request);
    TransactionResponse update(String username, Long id, TransactionRequest request);
    void delete(String username, Long id);
    TransactionResponse restore(Long id);
}
