package com.finance.tracker.repository;

import com.finance.tracker.model.FinanceTransaction;
import com.finance.tracker.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinanceTransactionRepository extends JpaRepository<FinanceTransaction, Long>,
        JpaSpecificationExecutor<FinanceTransaction> {

    Page<FinanceTransaction> findAllByUser_IdAndDeletedAtIsNull(Long userId, Pageable pageable);
    Optional<FinanceTransaction> findByIdAndUser_IdAndDeletedAtIsNull(Long id, Long userId);
    Optional<FinanceTransaction> findByIdAndDeletedAtIsNotNull(Long id);

    @Query("select coalesce(sum(t.amount), 0) from FinanceTransaction t " +
            "where t.user.id = :userId and t.deletedAt is null and t.type = :type " +
            "and t.transactionDate >= :from and t.transactionDate < :toExclusive and t.currency = :currency")
    BigDecimal sumByUserAndTypeAndDateRange(@Param("userId") Long userId,
            @Param("type") TransactionType type, @Param("from") LocalDate from,
            @Param("toExclusive") LocalDate toExclusive, @Param("currency") String currency);

    @Query("select t.category.id, t.category.name, coalesce(sum(t.amount), 0) " +
            "from FinanceTransaction t where t.user.id = :userId and t.deletedAt is null " +
            "and t.type = :type and t.transactionDate >= :from and t.transactionDate < :toExclusive " +
            "and t.currency = :currency " +
            "group by t.category.id, t.category.name order by t.category.name")
    List<Object[]> sumByCategoryAndDateRange(@Param("userId") Long userId,
            @Param("type") TransactionType type, @Param("from") LocalDate from,
            @Param("toExclusive") LocalDate toExclusive, @Param("currency") String currency);
}
