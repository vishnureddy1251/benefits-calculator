package com.paylocity.benefits_calculator.controller;

import com.paylocity.benefits_calculator.dto.ApiResponse;
import com.paylocity.benefits_calculator.dto.EmployeePaycheckDto;
import com.paylocity.benefits_calculator.entity.PayrollPeriod;
import com.paylocity.benefits_calculator.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for Payroll operations.
 *
 * Provides endpoints for:
 * - Pay period generation
 * - Paycheck calculations
 * - Paycheck generation and storage
 * - Paycheck history retrieval
 * - Bulk paycheck generation
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payroll", description = "Payroll and paycheck management endpoints")
public class PayrollController {

    private final PayrollService payrollService;

    /**
     * Generate pay periods for a year
     *
     * POST /api/v1/payroll/periods/{year}
     */
    @PostMapping("/periods/{year}")
    @Operation(summary = "Generate pay periods",
            description = "Generate 26 bi-weekly pay periods for a specific year")
    public ResponseEntity<ApiResponse<List<PayrollPeriod>>> generatePayPeriods(
            @Parameter(description = "Year (e.g., 2026)") @PathVariable int year) {

        log.info("Generating pay periods for year: {}", year);

        // Validate year
        if (year < 2020 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 2020 and 2100");
        }

        List<PayrollPeriod> periods = payrollService.generatePayPeriodsForYear(year);

        ApiResponse<List<PayrollPeriod>> response = ApiResponse.success(
                periods,
                String.format("Successfully generated %d pay periods for year %d", periods.size(), year)
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get current pay period
     *
     * GET /api/v1/payroll/periods/current
     */
    @GetMapping("/periods/current")
    @Operation(summary = "Get current pay period",
            description = "Get the pay period that contains the current date")
    public ResponseEntity<ApiResponse<PayrollPeriod>> getCurrentPayPeriod() {

        log.info("Fetching current pay period");

        PayrollPeriod period = payrollService.getCurrentPayPeriod();

        if (period == null) {
            ApiResponse<PayrollPeriod> response = ApiResponse.success(
                    null,
                    "No current pay period found. Generate pay periods first."
            );
            return ResponseEntity.ok(response);
        }

        ApiResponse<PayrollPeriod> response = ApiResponse.success(
                period,
                String.format("Current pay period: %s", period.getFormattedPeriod())
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get pay period count for a year
     *
     * GET /api/v1/payroll/periods/{year}/count
     */
    @GetMapping("/periods/{year}/count")
    @Operation(summary = "Get pay period count",
            description = "Get count of pay periods for a specific year")
    public ResponseEntity<ApiResponse<Long>> getPayPeriodCount(
            @Parameter(description = "Year") @PathVariable int year) {

        log.info("Fetching pay period count for year: {}", year);

        long count = payrollService.getPayPeriodCount(year);

        ApiResponse<Long> response = ApiResponse.success(
                count,
                String.format("Year %d has %d pay period(s)", year, count)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Calculate paycheck (without saving)
     *
     * GET /api/v1/payroll/calculate?employeeId={id}&payrollPeriodId={id}
     */
    @GetMapping("/calculate")
    @Operation(summary = "Calculate paycheck",
            description = "Calculate paycheck for an employee and pay period (does not save)")
    public ResponseEntity<ApiResponse<EmployeePaycheckDto>> calculatePaycheck(
            @Parameter(description = "Employee ID") @RequestParam Long employeeId,
            @Parameter(description = "Pay period ID") @RequestParam Long payrollPeriodId) {

        log.info("Calculating paycheck for employee {} and period {}", employeeId, payrollPeriodId);

        EmployeePaycheckDto paycheck = payrollService.calculatePaycheck(employeeId, payrollPeriodId);

        ApiResponse<EmployeePaycheckDto> response = ApiResponse.success(
                paycheck,
                String.format("Paycheck calculated for %s: $%s",
                        paycheck.getEmployeeFullName(), paycheck.getNetPay())
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Generate paycheck (calculate and save)
     *
     * POST /api/v1/payroll/paychecks
     */
    @PostMapping("/paychecks")
    @Operation(summary = "Generate paycheck",
            description = "Calculate and save paycheck for an employee and pay period")
    public ResponseEntity<ApiResponse<EmployeePaycheckDto>> generatePaycheck(
            @Parameter(description = "Employee ID") @RequestParam Long employeeId,
            @Parameter(description = "Pay period ID") @RequestParam Long payrollPeriodId) {

        log.info("Generating paycheck for employee {} and period {}", employeeId, payrollPeriodId);

        EmployeePaycheckDto paycheck = payrollService.generatePaycheck(employeeId, payrollPeriodId);

        ApiResponse<EmployeePaycheckDto> response = ApiResponse.success(
                paycheck,
                String.format("Paycheck generated and saved for %s: $%s",
                        paycheck.getEmployeeFullName(), paycheck.getNetPay())
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all paychecks for an employee
     *
     * GET /api/v1/payroll/employees/{employeeId}/paychecks
     */
    @GetMapping("/employees/{employeeId}/paychecks")
    @Operation(summary = "Get employee paychecks",
            description = "Get all paychecks for a specific employee")
    public ResponseEntity<ApiResponse<List<EmployeePaycheckDto>>> getEmployeePaychecks(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {

        log.info("Fetching paychecks for employee {}", employeeId);

        List<EmployeePaycheckDto> paychecks = payrollService.getEmployeePaychecks(employeeId);

        ApiResponse<List<EmployeePaycheckDto>> response = ApiResponse.success(
                paychecks,
                String.format("Employee has %d paycheck(s)", paychecks.size())
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get paycheck by ID
     *
     * GET /api/v1/payroll/paychecks/{id}
     */
    @GetMapping("/paychecks/{id}")
    @Operation(summary = "Get paycheck by ID",
            description = "Retrieve a specific paycheck by its ID")
    public ResponseEntity<ApiResponse<EmployeePaycheckDto>> getPaycheckById(
            @Parameter(description = "Paycheck ID") @PathVariable Long id) {

        log.info("Fetching paycheck with ID: {}", id);

        EmployeePaycheckDto paycheck = payrollService.getPaycheckById(id);

        ApiResponse<EmployeePaycheckDto> response = ApiResponse.success(
                paycheck,
                String.format("Paycheck for %s: $%s",
                        paycheck.getEmployeeFullName(), paycheck.getNetPay())
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Generate paychecks for all employees for a pay period
     *
     * POST /api/v1/payroll/periods/{payrollPeriodId}/generate
     */
    @PostMapping("/periods/{payrollPeriodId}/generate")
    @Operation(summary = "Generate all paychecks",
            description = "Generate paychecks for all active employees for a specific pay period")
    public ResponseEntity<ApiResponse<List<EmployeePaycheckDto>>> generateAllPaychecks(
            @Parameter(description = "Pay period ID") @PathVariable Long payrollPeriodId) {

        log.info("Generating paychecks for all employees for period {}", payrollPeriodId);

        List<EmployeePaycheckDto> paychecks = payrollService.generatePaychecksForAllEmployees(payrollPeriodId);

        ApiResponse<List<EmployeePaycheckDto>> response = ApiResponse.success(
                paychecks,
                String.format("Successfully generated %d paycheck(s)", paychecks.size())
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Calculate base pay per paycheck for an employee
     *
     * GET /api/v1/payroll/employees/{employeeId}/base-pay
     */
    @GetMapping("/employees/{employeeId}/base-pay")
    @Operation(summary = "Calculate base pay",
            description = "Calculate base pay per paycheck for an employee (before deductions)")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateBasePay(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {

        log.info("Calculating base pay for employee {}", employeeId);

        BigDecimal basePay = payrollService.calculateBasePayPerPaycheck(employeeId);

        ApiResponse<BigDecimal> response = ApiResponse.success(
                basePay,
                String.format("Base pay per paycheck: $%s", basePay)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Calculate benefit deductions for an employee
     *
     * GET /api/v1/payroll/employees/{employeeId}/benefit-deductions
     */
    @GetMapping("/employees/{employeeId}/benefit-deductions")
    @Operation(summary = "Calculate benefit deductions",
            description = "Calculate total benefit deductions per paycheck (employee + dependents)")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateBenefitDeductions(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {

        log.info("Calculating benefit deductions for employee {}", employeeId);

        BigDecimal deductions = payrollService.calculateBenefitDeductions(employeeId);

        ApiResponse<BigDecimal> response = ApiResponse.success(
                deductions,
                String.format("Benefit deductions per paycheck: $%s", deductions)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Calculate additional deductions (high salary surcharge)
     *
     * GET /api/v1/payroll/employees/{employeeId}/additional-deductions
     */
    @GetMapping("/employees/{employeeId}/additional-deductions")
    @Operation(summary = "Calculate additional deductions",
            description = "Calculate additional deductions (high salary surcharge if salary > $80k)")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateAdditionalDeductions(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {

        log.info("Calculating additional deductions for employee {}", employeeId);

        BigDecimal deductions = payrollService.calculateAdditionalDeductions(employeeId);

        ApiResponse<BigDecimal> response = ApiResponse.success(
                deductions,
                String.format("Additional deductions per paycheck: $%s", deductions)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Calculate net pay for an employee
     *
     * GET /api/v1/payroll/employees/{employeeId}/net-pay
     */
    @GetMapping("/employees/{employeeId}/net-pay")
    @Operation(summary = "Calculate net pay",
            description = "Calculate net pay (take-home) per paycheck for an employee")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateNetPay(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {

        log.info("Calculating net pay for employee {}", employeeId);

        BigDecimal netPay = payrollService.calculateNetPay(employeeId);

        ApiResponse<BigDecimal> response = ApiResponse.success(
                netPay,
                String.format("Net pay per paycheck: $%s", netPay)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Check if employee has high salary
     *
     * GET /api/v1/payroll/employees/{employeeId}/has-high-salary
     */
    @GetMapping("/employees/{employeeId}/has-high-salary")
    @Operation(summary = "Check high salary",
            description = "Check if employee's salary exceeds the high salary threshold ($80k)")
    public ResponseEntity<ApiResponse<Boolean>> hasHighSalary(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {

        log.info("Checking high salary status for employee {}", employeeId);

        boolean hasHighSalary = payrollService.hasHighSalary(employeeId);

        ApiResponse<Boolean> response = ApiResponse.success(
                hasHighSalary,
                hasHighSalary
                        ? "Employee has high salary (subject to 2% surcharge)"
                        : "Employee does not have high salary"
        );

        return ResponseEntity.ok(response);
    }
}