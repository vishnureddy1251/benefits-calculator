package com.paylocity.benefits_calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response structure for all API errors.
 *
 * Provides consistent error format across the entire API including:
 * - Timestamp of when error occurred
 * - HTTP status code
 * - Error message
 * - Request path
 * - Validation errors (if applicable)
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when the error occurred
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * HTTP status code (e.g., 400, 404, 500)
     */
    private int status;

    /**
     * Error type (e.g., "Bad Request", "Not Found")
     */
    private String error;

    /**
     * Detailed error message
     */
    private String message;

    /**
     * API path where error occurred
     */
    private String path;

    /**
     * Validation errors (field -> error message)
     * Only populated for validation failures
     */
    private Map<String, String> validationErrors;

    /**
     * Create error response without validation errors
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error,
                         String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    /**
     * Builder method for creating error responses
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                null
        );
    }

    /**
     * Builder method for creating validation error responses
     */
    public static ErrorResponse ofValidationErrors(String path, Map<String, String> errors) {
        return new ErrorResponse(
                LocalDateTime.now(),
                400,
                "Validation Failed",
                "Invalid input parameters",
                path,
                errors
        );
    }
}