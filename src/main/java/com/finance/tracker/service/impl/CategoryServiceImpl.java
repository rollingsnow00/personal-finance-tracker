package com.finance.tracker.service.impl;

import com.finance.tracker.config.PageRequestFactory;
import com.finance.tracker.dto.request.CategoryRequest;
import com.finance.tracker.dto.response.CategoryResponse;
import com.finance.tracker.exception.BusinessRuleException;
import com.finance.tracker.exception.DuplicateResourceException;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.mapper.CategoryMapper;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.CategoryType;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.service.CategoryService;
import com.finance.tracker.service.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final CurrentUserService currentUserService;

    public CategoryServiceImpl(CategoryRepository categoryRepository, BudgetRepository budgetRepository,
                               CurrentUserService currentUserService) {
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> list(String username, int page, int size, String sortBy, String direction) {
        User user = currentUserService.require(username);
        Pageable pageable = PageRequestFactory.create(page, size, sortBy, direction, Set.of("name", "createdAt", "updatedAt"));
        return categoryRepository.findAllByUser_IdAndDeletedAtIsNull(user.getId(), pageable).map(CategoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse get(String username, Long id) {
        return CategoryMapper.toResponse(requireOwnedActive(username, id));
    }

    @Override
    public CategoryResponse create(String username, CategoryRequest request) {
        User user = currentUserService.require(username);
        if (categoryRepository.existsByUser_IdAndNameIgnoreCaseAndDeletedAtIsNull(user.getId(), request.name().trim())) {
            throw new DuplicateResourceException("A category with this name already exists");
        }
        return CategoryMapper.toResponse(categoryRepository.save(
                new Category(request.name().trim(), request.type(), user)));
    }

    @Override
    public CategoryResponse update(String username, Long id, CategoryRequest request) {
        Category category = requireOwnedActive(username, id);
        if (categoryRepository.existsByUser_IdAndNameIgnoreCaseAndDeletedAtIsNullAndIdNot(
                category.getUser().getId(), request.name().trim(), id)) {
            throw new DuplicateResourceException("A category with this name already exists");
        }
        if (request.type() == CategoryType.INCOME && budgetRepository.existsByCategory_IdAndDeletedAtIsNull(id)) {
            throw new BusinessRuleException("A category with an active budget must remain an expense category");
        }
        category.update(request.name().trim(), request.type());
        return CategoryMapper.toResponse(category);
    }

    @Override
    public void delete(String username, Long id) {
        requireOwnedActive(username, id).markDeleted(Instant.now());
    }

    @Override
    public CategoryResponse restore(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNotNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted category", id));
        if (categoryRepository.existsByUser_IdAndNameIgnoreCaseAndDeletedAtIsNullAndIdNot(
                category.getUser().getId(), category.getName(), id)) {
            throw new DuplicateResourceException("Another active category uses this name; rename it before restoring");
        }
        category.restore();
        return CategoryMapper.toResponse(category);
    }

    private Category requireOwnedActive(String username, Long id) {
        User user = currentUserService.require(username);
        return categoryRepository.findByIdAndUser_IdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}
