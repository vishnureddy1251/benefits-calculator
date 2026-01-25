package com.paylocity.benefits_calculator.exception;

/**
 * Exception thrown when a business rule validation fails.
 *
 * Examples:
 * - Employee under 18 years old
 * - Employee already has a spouse/domestic partner
 * - Invalid salary range
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
public class BusinessValidationException extends RuntimeException {

    /**
     * Create exception with message
     *
     * @param message the error message
     */
    public BusinessValidationException(String message) {
        super(message);
    }

    /**
     * Create exception with message and cause
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}