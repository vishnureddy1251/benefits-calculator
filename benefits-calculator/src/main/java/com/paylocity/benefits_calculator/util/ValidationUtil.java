package com.paylocity.benefits_calculator.util;

import com.paylocity.benefits_calculator.exception.BusinessValidationException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

/**
 * Utility class for business rule validations.
 *
 * Contains reusable validation methods for:
 * - Age validation
 * - Salary validation
 * - Other business rules
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Slf4j
public class ValidationUtil {

    // Constants
    private static final int MINIMUM_EMPLOYEE_AGE = 18;
    private static final int MAXIMUM_EMPLOYEE_AGE = 100;
    private static final BigDecimal MINIMUM_SALARY = new BigDecimal("0.01");
    private static final BigDecimal MAXIMUM_SALARY = new BigDecimal("10000000.00");

    /**
     * Private constructor to prevent instantiation
     */
    private ValidationUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validate employee age
     *
     * @param dateOfBirth the employee's date of birth
     * @throws BusinessValidationException if age is invalid
     */
    public static void validateEmployeeAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new BusinessValidationException("Date of birth is required");
        }

        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Date of birth cannot be in the future");
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();

        if (age < MINIMUM_EMPLOYEE_AGE) {
            log.warn("Employee age validation failed: {} years old (minimum: {})",
                    age, MINIMUM_EMPLOYEE_AGE);
            throw new BusinessValidationException(
                    String.format("Employee must be at least %d years old. Current age: %d",
                            MINIMUM_EMPLOYEE_AGE, age)
            );
        }

        if (age > MAXIMUM_EMPLOYEE_AGE) {
            log.warn("Employee age validation failed: {} years old (maximum: {})",
                    age, MAXIMUM_EMPLOYEE_AGE);
            throw new BusinessValidationException(
                    String.format("Employee age cannot exceed %d years. Current age: %d",
                            MAXIMUM_EMPLOYEE_AGE, age)
            );
        }

        log.debug("Employee age validation passed: {} years old", age);
    }

    /**
     * Validate salary amount
     *
     * @param salary the salary to validate
     * @throws BusinessValidationException if salary is invalid
     */
    public static void validateSalary(BigDecimal salary) {
        if (salary == null) {
            throw new BusinessValidationException("Salary is required");
        }

        if (salary.compareTo(MINIMUM_SALARY) < 0) {
            log.warn("Salary validation failed: {} (minimum: {})", salary, MINIMUM_SALARY);
            throw new BusinessValidationException(
                    String.format("Salary must be at least %s. Provided: %s",
                            MINIMUM_SALARY, salary)
            );
        }

        if (salary.compareTo(MAXIMUM_SALARY) > 0) {
            log.warn("Salary validation failed: {} (maximum: {})", salary, MAXIMUM_SALARY);
            throw new BusinessValidationException(
                    String.format("Salary cannot exceed %s. Provided: %s",
                            MAXIMUM_SALARY, salary)
            );
        }

        log.debug("Salary validation passed: {}", salary);
    }

    /**
     * Validate dependent age
     *
     * @param dateOfBirth the dependent's date of birth
     * @throws BusinessValidationException if age is invalid
     */
    public static void validateDependentAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new BusinessValidationException("Date of birth is required");
        }

        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Date of birth cannot be in the future");
        }

        // No minimum age for dependents (can be newborn)
        // Maximum age validation could be added if needed
        log.debug("Dependent age validation passed");
    }

    /**
     * Calculate age from date of birth
     *
     * @param dateOfBirth the date of birth
     * @return age in years
     */
    public static int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}