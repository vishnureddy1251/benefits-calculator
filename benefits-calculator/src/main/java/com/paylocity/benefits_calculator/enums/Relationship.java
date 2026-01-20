package com.paylocity.benefits_calculator.enums;

/**
 * Enum representing the relationship of a dependent to an employee.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
public enum Relationship {
    /**
     * Relationship is not specified
     */
    NONE,

    /**
     * Dependent is a spouse
     */
    SPOUSE,

    /**
     * Dependent is a domestic partner
     */
    DOMESTIC_PARTNER,

    /**
     * Dependent is a child
     */
    CHILD
}