package com.paylocity.benefits_calculator.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model for pagination parameters in API requests.
 *
 * Provides page size and page number with validation to prevent
 * excessive or invalid pagination values.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor

public class PaginationFilter {

    /**
     * Page size (number of records per page)
     * Min: 1, Max: 100
     */
    private int pageSize;

    /**
     * Page number (1-based)
     * Min: 1
     */
    private int pageNumber;

    /**
     * Constructor with validation
     *
     * @param pageSize number of records per page (max 100)
     * @param pageNumber page number (min 1)
     */

    /**
     * Get the offset for database queries (0-based)
     * Formula: (pageNumber - 1) * pageSize
     *
     * @return the offset
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
}