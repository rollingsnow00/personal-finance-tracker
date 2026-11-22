package com.finance.tracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "categories", uniqueConstraints =
        @UniqueConstraint(name = "uk_category_owner_name", columnNames = {"user_id", "name"}))
public class Category extends SoftDeletableEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private CategoryType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Category() { }

    public Category(String name, CategoryType type, User user) {
        this.name = name;
        this.type = type;
        this.user = user;
    }

    public String getName() { return name; }
    public CategoryType getType() { return type; }
    public User getUser() { return user; }

    public void update(String name, CategoryType type) {
        this.name = name;
        this.type = type;
    }
}
