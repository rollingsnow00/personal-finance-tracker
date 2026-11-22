package com.finance.tracker;

import com.finance.tracker.dto.request.CategoryRequest;
import com.finance.tracker.dto.request.TransactionRequest;
import com.finance.tracker.exception.BusinessRuleException;
import com.finance.tracker.exception.DuplicateResourceException;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.CategoryType;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.FinanceTransactionRepository;
import com.finance.tracker.service.CurrentUserService;
import com.finance.tracker.service.impl.BudgetServiceImpl;
import com.finance.tracker.service.impl.CategoryServiceImpl;
import com.finance.tracker.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceLayerUnitTest {
    @Mock private CurrentUserService currentUserService;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private FinanceTransactionRepository transactionRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(10L);
    }

    @Test
    void categoryServiceRejectsAnActiveDuplicateName() {
        when(currentUserService.require("morris")).thenReturn(user);
        when(categoryRepository.existsByUser_IdAndNameIgnoreCaseAndDeletedAtIsNull(10L, "Food"))
                .thenReturn(true);
        CategoryServiceImpl service = new CategoryServiceImpl(categoryRepository, budgetRepository, currentUserService);

        assertThrows(DuplicateResourceException.class,
                () -> service.create("morris", new CategoryRequest("Food", CategoryType.EXPENSE)));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void transactionServiceRejectsCategoryTypeMismatch() {
        when(currentUserService.require("morris")).thenReturn(user);
        Category incomeCategory = org.mockito.Mockito.mock(Category.class);
        when(categoryRepository.findByIdAndUser_IdAndDeletedAtIsNull(5L, 10L)).thenReturn(Optional.of(incomeCategory));
        when(incomeCategory.getType()).thenReturn(CategoryType.INCOME);
        TransactionServiceImpl service = new TransactionServiceImpl(transactionRepository, categoryRepository, currentUserService);
        TransactionRequest request = new TransactionRequest(TransactionType.EXPENSE, new BigDecimal("25.00"),
                LocalDate.now(), "Cafe", "PLN", 5L);

        assertThrows(BusinessRuleException.class, () -> service.create("morris", request));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void budgetServiceOnlyAllowsExpenseCategories() {
        when(currentUserService.require("morris")).thenReturn(user);
        Category incomeCategory = org.mockito.Mockito.mock(Category.class);
        when(categoryRepository.findByIdAndUser_IdAndDeletedAtIsNull(5L, 10L)).thenReturn(Optional.of(incomeCategory));
        when(incomeCategory.getType()).thenReturn(CategoryType.INCOME);
        BudgetServiceImpl service = new BudgetServiceImpl(budgetRepository, categoryRepository, currentUserService);

        var request = new com.finance.tracker.dto.request.BudgetRequest(java.time.YearMonth.now(),
                new BigDecimal("250.00"), "PLN", 5L);
        assertThrows(BusinessRuleException.class, () -> service.create("morris", request));
        verify(budgetRepository, never()).save(any());
    }
}
