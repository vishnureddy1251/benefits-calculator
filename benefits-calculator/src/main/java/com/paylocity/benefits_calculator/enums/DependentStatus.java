package com.paylocity.benefits_calculator.enums;

/**
 * Enum representing the status of a dependent.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
public enum DependentStatus {
    /**
     * Status is not set or unknown
     */
    NONE,

    /**
     * Dependent is currently active
     */
    ACTIVE,

    /**
     * Dependent is no longer active (soft delete)
     */
    INACTIVE
}