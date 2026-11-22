package com.finance.tracker.service.impl;

import com.finance.tracker.config.PageRequestFactory;
import com.finance.tracker.dto.request.BudgetRequest;
import com.finance.tracker.dto.response.BudgetResponse;
import com.finance.tracker.exception.DuplicateResourceException;
import com.finance.tracker.exception.BusinessRuleException;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.mapper.BudgetMapper;
import com.finance.tracker.model.Budget;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.CategoryType;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.service.BudgetService;
import com.finance.tracker.service.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class BudgetServiceImpl implements BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public BudgetServiceImpl(BudgetRepository budgetRepository, CategoryRepository categoryRepository,
                             CurrentUserService currentUserService) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BudgetResponse> list(String username, int page, int size, String sortBy, String direction) {
        User user = currentUserService.require(username);
        Pageable pageable = PageRequestFactory.create(page, size, sortBy, direction,
                Set.of("monthStart", "limitAmount", "createdAt"));
        return budgetRepository.findAllByUser_IdAndDeletedAtIsNull(user.getId(), pageable).map(BudgetMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse get(String username, Long id) {
        return BudgetMapper.toResponse(requireOwnedActive(username, id));
    }

    @Override
    public BudgetResponse create(String username, BudgetRequest request) {
        User user = currentUserService.require(username);
        Category category = requireExpenseCategory(user.getId(), request.categoryId());
        var monthStart = request.month().atDay(1);
        if (budgetRepository.existsByUser_IdAndCategory_IdAndMonthStartAndDeletedAtIsNull(
                user.getId(), category.getId(), monthStart)) {
            throw new DuplicateResourceException("A budget already exists for this category and month");
        }
        Budget budget = budgetRepository.save(new Budget(monthStart, request.limitAmount(),
                request.currency().toUpperCase(Locale.ROOT), user, category));
        return BudgetMapper.toResponse(budget);
    }

    @Override
    public BudgetResponse update(String username, Long id, BudgetRequest request) {
        Budget budget = requireOwnedActive(username, id);
        Category category = requireExpenseCategory(budget.getUser().getId(), request.categoryId());
        var monthStart = request.month().atDay(1);
        if (!budget.getCategory().getId().equals(category.getId()) || !budget.getMonthStart().equals(monthStart)) {
            if (budgetRepository.existsByUser_IdAndCategory_IdAndMonthStartAndDeletedAtIsNull(
                    budget.getUser().getId(), category.getId(), monthStart)) {
                throw new DuplicateResourceException("A budget already exists for this category and month");
            }
        }
        budget.update(monthStart, request.limitAmount(), request.currency().toUpperCase(Locale.ROOT), category);
        return BudgetMapper.toResponse(budget);
    }

    @Override
    public void delete(String username, Long id) {
        requireOwnedActive(username, id).markDeleted(Instant.now());
    }

    @Override
    public BudgetResponse restore(Long id) {
        Budget budget = budgetRepository.findByIdAndDeletedAtIsNotNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted budget", id));
        if (budget.getCategory().isDeleted()) throw new ResourceNotFoundException("Category", budget.getCategory().getId());
        if (budgetRepository.existsByUser_IdAndCategory_IdAndMonthStartAndDeletedAtIsNull(
                budget.getUser().getId(), budget.getCategory().getId(), budget.getMonthStart())) {
            throw new DuplicateResourceException("An active budget already exists for this category and month");
        }
        budget.restore();
        return BudgetMapper.toResponse(budget);
    }

    private Budget requireOwnedActive(String username, Long id) {
        User user = currentUserService.require(username);
        return budgetRepository.findByIdAndUser_IdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
    }

    private Category requireExpenseCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findByIdAndUser_IdAndDeletedAtIsNull(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        if (category.getType() != CategoryType.EXPENSE) {
            throw new BusinessRuleException("Budgets can only be created for expense categories");
        }
        return category;
    }
}
