package com.finance.tracker.service.impl;

import com.finance.tracker.config.PageRequestFactory;
import com.finance.tracker.dto.request.TransactionRequest;
import com.finance.tracker.dto.request.TransactionSearchRequest;
import com.finance.tracker.dto.response.TransactionResponse;
import com.finance.tracker.exception.BusinessRuleException;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.mapper.TransactionMapper;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.FinanceTransaction;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.FinanceTransactionRepository;
import com.finance.tracker.service.CurrentUserService;
import com.finance.tracker.service.TransactionService;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final FinanceTransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public TransactionServiceImpl(FinanceTransactionRepository transactionRepository,
                                  CategoryRepository categoryRepository, CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> search(String username, TransactionSearchRequest filters,
                                            int page, int size, String sortBy, String direction) {
        User user = currentUserService.require(username);
        Pageable pageable = PageRequestFactory.create(page, size, sortBy, direction,
                Set.of("transactionDate", "amount", "createdAt", "type"));
        Specification<FinanceTransaction> specification = specification(user.getId(), filters);
        return transactionRepository.findAll(specification, pageable).map(TransactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse get(String username, Long id) {
        return TransactionMapper.toResponse(requireOwnedActive(username, id));
    }

    @Override
    public TransactionResponse create(String username, TransactionRequest request) {
        User user = currentUserService.require(username);
        Category category = requireCategory(user.getId(), request.categoryId(), request.type());
        FinanceTransaction created = transactionRepository.save(new FinanceTransaction(request.type(), request.amount(),
                request.transactionDate(), clean(request.description()), request.currency().toUpperCase(Locale.ROOT), user, category));
        log.info("Created transaction {} for user {}", created.getId(), username);
        return TransactionMapper.toResponse(created);
    }

    @Override
    public TransactionResponse update(String username, Long id, TransactionRequest request) {
        FinanceTransaction transaction = requireOwnedActive(username, id);
        Category category = requireCategory(transaction.getUser().getId(), request.categoryId(), request.type());
        transaction.update(request.type(), request.amount(), request.transactionDate(), clean(request.description()),
                request.currency().toUpperCase(Locale.ROOT), category);
        return TransactionMapper.toResponse(transaction);
    }

    @Override
    public void delete(String username, Long id) {
        requireOwnedActive(username, id).markDeleted(Instant.now());
        log.info("Soft-deleted transaction {} for user {}", id, username);
    }

    @Override
    public TransactionResponse restore(Long id) {
        FinanceTransaction transaction = transactionRepository.findByIdAndDeletedAtIsNotNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted transaction", id));
        if (transaction.getCategory().isDeleted()) {
            throw new BusinessRuleException("Restore the transaction's category first");
        }
        transaction.restore();
        return TransactionMapper.toResponse(transaction);
    }

    private FinanceTransaction requireOwnedActive(String username, Long id) {
        User user = currentUserService.require(username);
        return transactionRepository.findByIdAndUser_IdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }

    private Category requireCategory(Long userId, Long categoryId, com.finance.tracker.model.TransactionType type) {
        Category category = categoryRepository.findByIdAndUser_IdAndDeletedAtIsNull(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        if (!category.getType().name().equals(type.name())) {
            throw new BusinessRuleException("Transaction type must match the category type");
        }
        return category;
    }

    private Specification<FinanceTransaction> specification(Long userId, TransactionSearchRequest filters) {
        return (root, query, builder) -> {
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("category", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("user").get("id"), userId));
            predicates.add(builder.isNull(root.get("deletedAt")));
            if (filters.from() != null) predicates.add(builder.greaterThanOrEqualTo(root.get("transactionDate"), filters.from()));
            if (filters.to() != null) predicates.add(builder.lessThanOrEqualTo(root.get("transactionDate"), filters.to()));
            if (filters.type() != null) predicates.add(builder.equal(root.get("type"), filters.type()));
            if (filters.categoryId() != null) predicates.add(builder.equal(root.get("category").get("id"), filters.categoryId()));
            if (filters.minAmount() != null) predicates.add(builder.greaterThanOrEqualTo(root.get("amount"), filters.minAmount()));
            if (filters.maxAmount() != null) predicates.add(builder.lessThanOrEqualTo(root.get("amount"), filters.maxAmount()));
            if (filters.query() != null && !filters.query().isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("description")),
                        "%" + filters.query().trim().toLowerCase(Locale.ROOT) + "%"));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String clean(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
