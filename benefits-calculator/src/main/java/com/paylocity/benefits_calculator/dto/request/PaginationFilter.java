package com.paylocity.benefits_calculator.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pagination filter for list queries.
 *
 * Provides page number and page size with automatic validation.
 * Page numbers are 1-based (user-friendly), converted to 0-based for Spring.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Getter
@NoArgsConstructor
public class PaginationFilter {

    /**
     * Page number (1-based, user-friendly)
     * Automatically clamped to minimum of 1
     */
    private int pageNumber = 1;

    /**
     * Number of items per page
     * Automatically clamped between 1 and 100
     */
    private int pageSize = 10;

    /**
     * Create pagination filter with validation
     *
     * @param pageNumber page number (minimum 1)
     * @param pageSize page size (1-100)
     */
    public PaginationFilter(Integer pageNumber, Integer pageSize) {
        // Validate and set page number (minimum 1)
        this.pageNumber = (pageNumber != null && pageNumber > 0) ? pageNumber : 1;

        // Validate and set page size (1-100)
        if (pageSize != null) {
            this.pageSize = Math.max(1, Math.min(100, pageSize));
        }
    }

    /**
     * Get offset for database queries (0-based)
     *
     * @return offset for database
     */
    public int getOffset() {
        return (pageNumber - 1) * pageSize;
    }

    /**
     * Check if this is the first page
     *
     * @return true if page number is 1
     */
    public boolean isFirstPage() {
        return pageNumber == 1;
    }

    /**
     * Get page number in 0-based format (for Spring Data)
     *
     * @return 0-based page number
     */
    public int getPageNumberZeroBased() {
        return pageNumber - 1;
    }
}