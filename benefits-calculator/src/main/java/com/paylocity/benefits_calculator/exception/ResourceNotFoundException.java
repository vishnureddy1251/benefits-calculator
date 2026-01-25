package com.paylocity.benefits_calculator.exception;

/**
 * Exception thrown when a requested resource is not found.
 *
 * This is a preview of the exception handling we'll build fully in Day 12.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Create exception with message
     *
     * @param message the error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Create exception with message and cause
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create exception for a specific resource type and ID
     *
     * @param resourceName the name of the resource (e.g., "Employee")
     * @param fieldName the field used to search (e.g., "id")
     * @param fieldValue the value that was searched for
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}