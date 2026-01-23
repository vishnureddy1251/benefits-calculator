package com.paylocity.benefits_calculator.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a Payroll Period (bi-weekly pay period).
 *
 * The company pays employees bi-weekly (26 pay periods per year).
 * This entity tracks each pay period with start and end dates.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Entity
@Table(name = "payroll_periods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollPeriod extends BaseEntity {

    /**
     * Start date of the pay period
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * End date of the pay period (typically 13 days after start date for bi-weekly)
     */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Constructor for creating a pay period with specific dates
     *
     * @param startDate the start date of the pay period
     * @param endDate the end date of the pay period
     */
    /**
    public PayrollPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }*/

    /**
     * Check if a given date falls within this pay period
     *
     * @param date the date to check
     * @return true if the date is within this pay period
     */
    public boolean contains(LocalDateTime date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Check if this is the current pay period
     *
     * @return true if current date falls within this pay period
     */
    public boolean isCurrent() {
        return contains(LocalDateTime.now());
    }

    /**
     * Get the number of days in this pay period
     *
     * @return number of days
     */
    public long getDurationInDays() {
        return java.time.Duration.between(startDate, endDate).toDays() + 1;
    }

    /**
     * Format the pay period as a string (for display purposes)
     *
     * @return formatted string like "2024-01-01 to 2024-01-14"
     */
    public String getFormattedPeriod() {
        return startDate.toLocalDate() + " to " + endDate.toLocalDate();
    }

    /**
     * Check if this pay period overlaps with another
     *
     * @param other the other pay period to check
     * @return true if there is any overlap
     */
    public boolean overlaps(PayrollPeriod other) {
        return !this.endDate.isBefore(other.startDate) &&
                !other.endDate.isBefore(this.startDate);
    }
}