package com.paylocity.benefits_calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Employee Paycheck.
 *
 * Represents a calculated paycheck for an employee for a specific pay period.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePaycheckDto {

    /**
     * Paycheck ID
     */
    private Long id;

    /**
     * Net pay (take-home amount after all deductions)
     */
    private BigDecimal netPay;

    /**
     * Benefits deductions
     */
    private BigDecimal benefitDeductions;

    /**
     * Additional deductions (e.g., high salary surcharge)
     */
    private BigDecimal additionalDeductions;

    /**
     * Gross pay (before deductions)
     */
    private BigDecimal grossPay;

    /**
     * Employee's first name
     */
    private String employeeFirstName;

    /**
     * Employee's last name
     */
    private String employeeLastName;

    /**
     * Pay period start date
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    /**
     * Pay period end date
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    /**
     * Get employee's full name
     *
     * @return full name
     */
    public String getEmployeeFullName() {
        return employeeFirstName + " " + employeeLastName;
    }

    /**
     * Get total deductions
     *
     * @return sum of all deductions
     */
    public BigDecimal getTotalDeductions() {
        return benefitDeductions.add(additionalDeductions);
    }
}