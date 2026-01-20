package com.paylocity.benefits_calculator.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base entity class that provides common fields for all entities.
 * Includes audit fields (createdDate, modifiedDate) and ID field.
 *
 * This class uses JPA Auditing to automatically populate audit fields.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * Primary key identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Timestamp when the entity was created.
     * Automatically populated by JPA auditing.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * Timestamp when the entity was last modified.
     * Automatically updated by JPA auditing.
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    /**
     * Pre-persist hook to set initial timestamps if not already set.
     * This ensures timestamps are set even if auditing is not fully configured.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdDate == null) {
            createdDate = now;
        }
        if (modifiedDate == null) {
            modifiedDate = now;
        }
    }

    /**
     * Pre-update hook to update the modified timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}