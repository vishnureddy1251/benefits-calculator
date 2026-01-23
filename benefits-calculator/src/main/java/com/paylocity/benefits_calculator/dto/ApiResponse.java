package com.paylocity.benefits_calculator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic API response wrapper for all REST endpoints.
 *
 * Provides a consistent response structure across all API endpoints with:
 * - Data payload (generic type T)
 * - Success indicator
 * - Success/error messages
 *
 * @param <T> The type of data being returned
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * The actual data payload
     */
    private T data;

    /**
     * Indicates if the operation was successful
     */
    private boolean success = true;

    /**
     * Success or informational message
     */
    private String message = "";

    /**
     * Error message (only populated when success = false)
     */
    private String error = "";

    /**
     * Create a successful response with data
     *
     * @param data the response data
     * @param <T> the type of data
     * @return ApiResponse with success = true
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setData(data);
        response.setSuccess(true);
        return response;
    }

    /**
     * Create a successful response with data and message
     *
     * @param data the response data
     * @param message success message
     * @param <T> the type of data
     * @return ApiResponse with success = true
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setData(data);
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    /**
     * Create an error response
     *
     * @param error error message
     * @param <T> the type of data
     * @return ApiResponse with success = false
     */
    public static <T> ApiResponse<T> error(String error) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }

    /**
     * Create an error response with custom message
     *
     * @param error error message
     * @param message additional context message
     * @param <T> the type of data
     * @return ApiResponse with success = false
     */
    public static <T> ApiResponse<T> error(String error, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setError(error);
        response.setMessage(message);
        return response;
    }
}