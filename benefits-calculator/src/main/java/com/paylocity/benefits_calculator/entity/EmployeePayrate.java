package com.paylocity.benefits_calculator.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an Employee's salary/payrate record.
 *
 * This tracks the salary history of an employee, allowing for salary changes over time.
 * Each record has a start and end date to define when that salary was/is effective.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Entity
@Table(name = "employee_payrates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePayrate extends BaseEntity {

    /**
     * Reference to the employee this payrate belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Base salary amount (annual)
     */
    @Column(name = "base_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseSalary;

    /**
     * Start date when this salary becomes effective
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * End date when this salary is no longer effective.
     * Use a far future date (e.g., LocalDateTime.MAX) for current salary.
     */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Constructor for creating a new payrate with employee reference
     *
     * @param employee the employee this payrate belongs to
     * @param baseSalary the annual salary amount
     * @param startDate when this salary starts
     * @param endDate when this salary ends
     */

    /**
    public EmployeePayrate(Employee employee, BigDecimal baseSalary,
                           LocalDateTime startDate, LocalDateTime endDate) {
        this.employee = employee;
        this.baseSalary = baseSalary;
        this.startDate = startDate;
        this.endDate = endDate;
    }
     */

    /**
     * Check if this payrate is currently active based on the given date
     *
     * @param checkDate the date to check against
     * @return true if this payrate is active on the given date
     */
    public boolean isActiveOn(LocalDateTime checkDate) {
        return !checkDate.isBefore(startDate) && !checkDate.isAfter(endDate);
    }

    /**
     * Check if this payrate is currently active (based on current date/time)
     *
     * @return true if this payrate is currently active
     */
    public boolean isCurrentlyActive() {
        return isActiveOn(LocalDateTime.now());
    }
}