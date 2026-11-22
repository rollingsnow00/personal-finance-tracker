package com.finance.tracker.repository;

import com.finance.tracker.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findAllByUser_IdAndDeletedAtIsNull(Long userId, Pageable pageable);
    Optional<Category> findByIdAndUser_IdAndDeletedAtIsNull(Long id, Long userId);
    Optional<Category> findByIdAndDeletedAtIsNotNull(Long id);
    boolean existsByUser_IdAndNameIgnoreCaseAndDeletedAtIsNull(Long userId, String name);
    boolean existsByUser_IdAndNameIgnoreCaseAndDeletedAtIsNullAndIdNot(Long userId, String name, Long excludedId);
}
