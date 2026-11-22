package com.finance.tracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "budgets", uniqueConstraints = @UniqueConstraint(
        name = "uk_budget_owner_category_month", columnNames = {"user_id", "category_id", "month_start"}),
        indexes = @Index(name = "idx_budget_user_month", columnList = "user_id, month_start"))
public class Budget extends SoftDeletableEntity {

    @Column(name = "month_start", nullable = false)
    private LocalDate monthStart;

    @Column(name = "limit_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal limitAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected Budget() { }

    public Budget(LocalDate monthStart, BigDecimal limitAmount, String currency, User user, Category category) {
        this.monthStart = monthStart.withDayOfMonth(1);
        this.limitAmount = limitAmount;
        this.currency = currency;
        this.user = user;
        this.category = category;
    }

    public LocalDate getMonthStart() { return monthStart; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public String getCurrency() { return currency; }
    public User getUser() { return user; }
    public Category getCategory() { return category; }

    public void update(LocalDate monthStart, BigDecimal limitAmount, String currency, Category category) {
        this.monthStart = monthStart.withDayOfMonth(1);
        this.limitAmount = limitAmount;
        this.currency = currency;
        this.category = category;
    }
}
