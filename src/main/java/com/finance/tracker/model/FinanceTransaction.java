package com.finance.tracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "finance_transactions", indexes = {
        @Index(name = "idx_transaction_user_date", columnList = "user_id, transaction_date"),
        @Index(name = "idx_transaction_category", columnList = "category_id")
})
public class FinanceTransaction extends SoftDeletableEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TransactionType type;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 3)
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected FinanceTransaction() { }

    public FinanceTransaction(TransactionType type, BigDecimal amount, LocalDate transactionDate,
                               String description, String currency, User user, Category category) {
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
        this.currency = currency;
        this.user = user;
        this.category = category;
    }

    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public String getDescription() { return description; }
    public String getCurrency() { return currency; }
    public User getUser() { return user; }
    public Category getCategory() { return category; }

    public void update(TransactionType type, BigDecimal amount, LocalDate transactionDate,
                       String description, String currency, Category category) {
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
        this.currency = currency;
        this.category = category;
    }
}
