package com.paylocity.benefits_calculator.service;

import com.paylocity.benefits_calculator.dto.EmployeePaycheckDto;
import com.paylocity.benefits_calculator.entity.PayrollPeriod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Payroll operations.
 *
 * Handles all payroll-related business logic including:
 * - Pay period generation
 * - Paycheck calculations
 * - Benefit deductions
 * - High salary surcharges
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
public interface PayrollService {

    /**
     * Generate pay periods for a specific year
     * Creates 26 bi-weekly pay periods
     *
     * @param year the year to generate pay periods for
     * @return list of created pay periods
     */
    List<PayrollPeriod> generatePayPeriodsForYear(int year);

    /**
     * Calculate paycheck for an employee for a specific pay period
     *
     * @param employeeId the employee ID
     * @param payrollPeriodId the pay period ID
     * @return calculated paycheck DTO
     */
    EmployeePaycheckDto calculatePaycheck(Long employeeId, Long payrollPeriodId);

    /**
     * Calculate and save paycheck for an employee for a specific pay period
     *
     * @param employeeId the employee ID
     * @param payrollPeriodId the pay period ID
     * @return saved paycheck DTO
     */
    EmployeePaycheckDto generatePaycheck(Long employeeId, Long payrollPeriodId);

    /**
     * Get all paychecks for an employee
     *
     * @param employeeId the employee ID
     * @return list of paycheck DTOs
     */
    List<EmployeePaycheckDto> getEmployeePaychecks(Long employeeId);

    /**
     * Get paycheck by ID
     *
     * @param paycheckId the paycheck ID
     * @return paycheck DTO
     */
    EmployeePaycheckDto getPaycheckById(Long paycheckId);

    /**
     * Get current pay period
     *
     * @return current pay period or null if not found
     */
    PayrollPeriod getCurrentPayPeriod();

    /**
     * Calculate employee's base pay per paycheck (before deductions)
     *
     * @param employeeId the employee ID
     * @return base pay amount
     */
    BigDecimal calculateBasePayPerPaycheck(Long employeeId);

    /**
     * Calculate total benefit deductions per paycheck
     * Includes employee and all dependent benefits
     *
     * @param employeeId the employee ID
     * @return total benefit deductions
     */
    BigDecimal calculateBenefitDeductions(Long employeeId);

    /**
     * Calculate additional deductions (e.g., high salary surcharge)
     *
     * @param employeeId the employee ID
     * @return additional deductions
     */
    BigDecimal calculateAdditionalDeductions(Long employeeId);

    /**
     * Calculate net pay (take-home) for an employee
     *
     * @param employeeId the employee ID
     * @return net pay amount
     */
    BigDecimal calculateNetPay(Long employeeId);

    /**
     * Generate paychecks for all active employees for a specific pay period
     *
     * @param payrollPeriodId the pay period ID
     * @return list of generated paycheck DTOs
     */
    List<EmployeePaycheckDto> generatePaychecksForAllEmployees(Long payrollPeriodId);

    /**
     * Check if employee has high salary (over threshold)
     *
     * @param employeeId the employee ID
     * @return true if salary is over high salary threshold
     */
    boolean hasHighSalary(Long employeeId);

    /**
     * Get count of pay periods for a year
     *
     * @param year the year
     * @return count of pay periods
     */
    long getPayPeriodCount(int year);
}