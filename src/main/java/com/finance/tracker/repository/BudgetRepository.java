package com.finance.tracker.repository;

import com.finance.tracker.model.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    @EntityGraph(attributePaths = "category")
    Page<Budget> findAllByUser_IdAndDeletedAtIsNull(Long userId, Pageable pageable);
    Optional<Budget> findByIdAndUser_IdAndDeletedAtIsNull(Long id, Long userId);
    Optional<Budget> findByIdAndDeletedAtIsNotNull(Long id);
    @EntityGraph(attributePaths = "category")
    List<Budget> findAllByUser_IdAndMonthStartAndDeletedAtIsNull(Long userId, LocalDate monthStart);
    boolean existsByUser_IdAndCategory_IdAndMonthStartAndDeletedAtIsNull(Long userId, Long categoryId, LocalDate monthStart);
    boolean existsByCategory_IdAndDeletedAtIsNull(Long categoryId);
}
