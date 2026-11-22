package com.finance.tracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
public abstract class SoftDeletableEntity extends AuditedEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Instant getDeletedAt() { return deletedAt; }
    public boolean isDeleted() { return deletedAt != null; }

    public void markDeleted(Instant deletedAt) { this.deletedAt = deletedAt; }
    public void restore() { this.deletedAt = null; }
}
