package com.paylocity.benefits_calculator.service.impl;

import com.paylocity.benefits_calculator.dto.EmployeePaycheckDto;
import com.paylocity.benefits_calculator.entity.*;
import com.paylocity.benefits_calculator.enums.DependentStatus;
import com.paylocity.benefits_calculator.enums.EmployeeStatus;
import com.paylocity.benefits_calculator.exception.BusinessValidationException;
import com.paylocity.benefits_calculator.exception.ResourceNotFoundException;
import com.paylocity.benefits_calculator.repository.DependentRepository;
import com.paylocity.benefits_calculator.repository.EmployeePayPeriodRepository;
import com.paylocity.benefits_calculator.repository.EmployeeRepository;
import com.paylocity.benefits_calculator.repository.PayrollPeriodRepository;
import com.paylocity.benefits_calculator.service.PayrollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Complete implementation of PayrollService with persistence.
 *
 * Handles all payroll operations including:
 * - Pay period generation
 * - Paycheck calculations
 * - Saving paychecks to database
 * - Retrieving paycheck history
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final EmployeeRepository employeeRepository;
    private final DependentRepository dependentRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final EmployeePayPeriodRepository employeePayPeriodRepository;
    private final ModelMapper modelMapper;

    // Configuration values from application.yml
    @Value("${app.payroll.employee-benefit-cost-monthly:1000.00}")
    private BigDecimal employeeBenefitCostMonthly;

    @Value("${app.payroll.dependent-benefit-cost-monthly-under-50:600.00}")
    private BigDecimal dependentBenefitCostUnder50;

    @Value("${app.payroll.dependent-benefit-cost-monthly-over-50:800.00}")
    private BigDecimal dependentBenefitCostOver50;

    @Value("${app.payroll.high-salary-threshold:80000.00}")
    private BigDecimal highSalaryThreshold;

    @Value("${app.payroll.high-salary-additional-percentage:0.02}")
    private BigDecimal highSalaryAdditionalPercentage;

    @Value("${app.payroll.paychecks-per-year:26}")
    private int paychecksPerYear;

    private static final int DAYS_PER_PAY_PERIOD = 14;

    @Override
    public List<PayrollPeriod> generatePayPeriodsForYear(int year) {
        log.info("Generating pay periods for year: {}", year);

        // Check if pay periods already exist for this year
        LocalDateTime yearStart = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime yearEnd = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        long existingCount = payrollPeriodRepository.countPayPeriodsInYear(yearStart, yearEnd);
        if (existingCount > 0) {
            log.warn("Pay periods already exist for year {}. Count: {}", year, existingCount);
            throw new BusinessValidationException(
                    String.format("Pay periods already exist for year %d. Found %d periods.", year, existingCount)
            );
        }

        List<PayrollPeriod> payPeriods = new ArrayList<>();
        LocalDateTime startDate = yearStart;

        // Generate 26 bi-weekly pay periods
        for (int i = 0; i < paychecksPerYear; i++) {
            LocalDateTime endDate = startDate.plusDays(DAYS_PER_PAY_PERIOD - 1)
                    .with(LocalTime.of(23, 59, 59));

            PayrollPeriod payPeriod = new PayrollPeriod(startDate, endDate);
            payPeriods.add(payPeriod);

            // Next period starts the day after this one ends
            startDate = endDate.plusDays(1).with(LocalTime.of(0, 0, 0));
        }

        // Save all pay periods
        List<PayrollPeriod> savedPeriods = payrollPeriodRepository.saveAll(payPeriods);

        log.info("Successfully generated {} pay periods for year {}", savedPeriods.size(), year);

        return savedPeriods;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePaycheckDto calculatePaycheck(Long employeeId, Long payrollPeriodId) {
        log.info("Calculating paycheck for employee {} for pay period {}", employeeId, payrollPeriodId);

        // Verify employee exists
        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(employeeId, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        // Verify pay period exists
        PayrollPeriod payrollPeriod = payrollPeriodRepository
                .findById(payrollPeriodId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollPeriod", "id", payrollPeriodId));

        // Calculate paycheck components
        BigDecimal basePay = calculateBasePayPerPaycheck(employeeId);
        BigDecimal benefitDeductions = calculateBenefitDeductions(employeeId);
        BigDecimal additionalDeductions = calculateAdditionalDeductions(employeeId);
        BigDecimal netPay = basePay.subtract(benefitDeductions).subtract(additionalDeductions);

        // Create DTO
        EmployeePaycheckDto dto = new EmployeePaycheckDto();
        dto.setGrossPay(basePay);
        dto.setBenefitDeductions(benefitDeductions);
        dto.setAdditionalDeductions(additionalDeductions);
        dto.setNetPay(netPay);
        dto.setEmployeeFirstName(employee.getFirstName());
        dto.setEmployeeLastName(employee.getLastName());
        dto.setStartDate(payrollPeriod.getStartDate());
        dto.setEndDate(payrollPeriod.getEndDate());

        log.info("Paycheck calculated: Gross=${}, Net=${}", basePay, netPay);

        return dto;
    }

    @Override
    public EmployeePaycheckDto generatePaycheck(Long employeeId, Long payrollPeriodId) {
        log.info("Generating and saving paycheck for employee {} for pay period {}",
                employeeId, payrollPeriodId);

        // Check if paycheck already exists
        if (employeePayPeriodRepository.existsByEmployee_IdAndPayrollPeriod_Id(
                employeeId, payrollPeriodId)) {
            throw new BusinessValidationException(
                    String.format("Paycheck already exists for employee %d and pay period %d",
                            employeeId, payrollPeriodId)
            );
        }

        // Get employee and pay period
        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(employeeId, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        PayrollPeriod payrollPeriod = payrollPeriodRepository
                .findById(payrollPeriodId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollPeriod", "id", payrollPeriodId));

        // Calculate paycheck
        BigDecimal basePay = calculateBasePayPerPaycheck(employeeId);
        BigDecimal benefitDeductions = calculateBenefitDeductions(employeeId);
        BigDecimal additionalDeductions = calculateAdditionalDeductions(employeeId);
        BigDecimal netPay = basePay.subtract(benefitDeductions).subtract(additionalDeductions);

        // Create EmployeePayPeriod entity
        EmployeePayPeriod employeePayPeriod = new EmployeePayPeriod();
        employeePayPeriod.setEmployee(employee);
        employeePayPeriod.setPayrollPeriod(payrollPeriod);
        employeePayPeriod.setBaseAmount(basePay);
        employeePayPeriod.setBenefitsAmount(benefitDeductions);
        employeePayPeriod.setAdditionalBenefitCost(additionalDeductions);
        employeePayPeriod.setTotalAmount(netPay);

        // Save to database
        EmployeePayPeriod savedPaycheck = employeePayPeriodRepository.save(employeePayPeriod);

        log.info("Paycheck saved with ID: {}", savedPaycheck.getId());

        // Convert to DTO
        return convertToDto(savedPaycheck);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeePaycheckDto> getEmployeePaychecks(Long employeeId) {
        log.info("Fetching all paychecks for employee {}", employeeId);

        // Verify employee exists
        if (!employeeRepository.existsByIdAndEmployeeStatus(employeeId, EmployeeStatus.ACTIVE)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        List<EmployeePayPeriod> paychecks = employeePayPeriodRepository
                .findByEmployee_Id(employeeId);

        log.info("Found {} paychecks for employee {}", paychecks.size(), employeeId);

        return paychecks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePaycheckDto getPaycheckById(Long paycheckId) {
        log.info("Fetching paycheck with ID: {}", paycheckId);

        EmployeePayPeriod paycheck = employeePayPeriodRepository
                .findById(paycheckId)
                .orElseThrow(() -> new ResourceNotFoundException("Paycheck", "id", paycheckId));

        return convertToDto(paycheck);
    }

    @Override
    public List<EmployeePaycheckDto> generatePaychecksForAllEmployees(Long payrollPeriodId) {
        log.info("Generating paychecks for all employees for pay period {}", payrollPeriodId);

        // Verify pay period exists
        PayrollPeriod payrollPeriod = payrollPeriodRepository
                .findById(payrollPeriodId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollPeriod", "id", payrollPeriodId));

        // Get all active employees
        List<Employee> employees = employeeRepository.findByEmployeeStatus(EmployeeStatus.ACTIVE);

        log.info("Generating paychecks for {} employees", employees.size());

        List<EmployeePaycheckDto> paychecks = new ArrayList<>();

        for (Employee employee : employees) {
            try {
                // Skip if paycheck already exists
                if (employeePayPeriodRepository.existsByEmployee_IdAndPayrollPeriod_Id(
                        employee.getId(), payrollPeriodId)) {
                    log.warn("Paycheck already exists for employee {}, skipping", employee.getId());
                    continue;
                }

                // Generate paycheck
                EmployeePaycheckDto paycheck = generatePaycheck(employee.getId(), payrollPeriodId);
                paychecks.add(paycheck);

                log.debug("Generated paycheck for employee {}: ${}",
                        employee.getId(), paycheck.getNetPay());

            } catch (Exception e) {
                log.error("Failed to generate paycheck for employee {}: {}",
                        employee.getId(), e.getMessage());
                // Continue with other employees
            }
        }

        log.info("Successfully generated {} paychecks", paychecks.size());

        return paychecks;
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollPeriod getCurrentPayPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return payrollPeriodRepository.findCurrentPayPeriod(now).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateBasePayPerPaycheck(Long employeeId) {
        log.debug("Calculating base pay per paycheck for employee {}", employeeId);

        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(employeeId, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        // Get current salary
        BigDecimal annualSalary = getCurrentSalary(employee);

        // Divide by number of paychecks per year
        BigDecimal basePayPerPaycheck = annualSalary
                .divide(BigDecimal.valueOf(paychecksPerYear), 2, RoundingMode.HALF_UP);

        log.debug("Base pay per paycheck: ${} (Annual: ${})", basePayPerPaycheck, annualSalary);

        return basePayPerPaycheck;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateBenefitDeductions(Long employeeId) {
        log.debug("Calculating benefit deductions for employee {}", employeeId);

        // Employee base benefit cost per paycheck
        BigDecimal employeeBenefitPerPaycheck = employeeBenefitCostMonthly
                .multiply(BigDecimal.valueOf(12))
                .divide(BigDecimal.valueOf(paychecksPerYear), 2, RoundingMode.HALF_UP);

        log.debug("Employee benefit per paycheck: ${}", employeeBenefitPerPaycheck);

        // Get dependents and calculate their costs
        List<Dependent> dependents = dependentRepository
                .findByEmployee_IdAndDependentStatus(employeeId, DependentStatus.ACTIVE);

        BigDecimal dependentBenefitTotal = BigDecimal.ZERO;

        for (Dependent dependent : dependents) {
            BigDecimal monthlyCost = dependent.isOver50()
                    ? dependentBenefitCostOver50
                    : dependentBenefitCostUnder50;

            BigDecimal costPerPaycheck = monthlyCost
                    .multiply(BigDecimal.valueOf(12))
                    .divide(BigDecimal.valueOf(paychecksPerYear), 2, RoundingMode.HALF_UP);

            dependentBenefitTotal = dependentBenefitTotal.add(costPerPaycheck);

            log.debug("Dependent {} ({} years old): ${}/paycheck",
                    dependent.getFullName(),
                    dependent.getAge(),
                    costPerPaycheck);
        }

        BigDecimal totalBenefitDeductions = employeeBenefitPerPaycheck.add(dependentBenefitTotal);

        log.debug("Total benefit deductions: ${} (Employee: ${}, Dependents: ${})",
                totalBenefitDeductions, employeeBenefitPerPaycheck, dependentBenefitTotal);

        return totalBenefitDeductions;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateAdditionalDeductions(Long employeeId) {
        log.debug("Calculating additional deductions for employee {}", employeeId);

        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(employeeId, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        BigDecimal annualSalary = getCurrentSalary(employee);

        // Check if salary exceeds threshold
        if (annualSalary.compareTo(highSalaryThreshold) > 0) {
            // Calculate 2% of annual salary, then divide by paychecks per year
            BigDecimal additionalCost = annualSalary
                    .multiply(highSalaryAdditionalPercentage)
                    .divide(BigDecimal.valueOf(paychecksPerYear), 2, RoundingMode.HALF_UP);

            log.debug("High salary surcharge applied: ${}/paycheck ({}% of ${})",
                    additionalCost, highSalaryAdditionalPercentage.multiply(BigDecimal.valueOf(100)), annualSalary);

            return additionalCost;
        }

        log.debug("No additional deductions (salary ${} <= threshold ${})",
                annualSalary, highSalaryThreshold);

        return BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateNetPay(Long employeeId) {
        BigDecimal basePay = calculateBasePayPerPaycheck(employeeId);
        BigDecimal benefitDeductions = calculateBenefitDeductions(employeeId);
        BigDecimal additionalDeductions = calculateAdditionalDeductions(employeeId);

        return basePay.subtract(benefitDeductions).subtract(additionalDeductions);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasHighSalary(Long employeeId) {
        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(employeeId, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        BigDecimal salary = getCurrentSalary(employee);
        return salary.compareTo(highSalaryThreshold) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long getPayPeriodCount(int year) {
        LocalDateTime yearStart = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime yearEnd = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        return payrollPeriodRepository.countPayPeriodsInYear(yearStart, yearEnd);
    }

    /**
     * Get current salary for an employee
     */
    private BigDecimal getCurrentSalary(Employee employee) {
        LocalDateTime now = LocalDateTime.now();

        return employee.getEmployeePayrates().stream()
                .filter(payrate -> payrate.isActiveOn(now))
                .findFirst()
                .map(EmployeePayrate::getBaseSalary)
                .orElseThrow(() -> new BusinessValidationException(
                        "No active salary found for employee " + employee.getId()));
    }

    /**
     * Convert EmployeePayPeriod entity to DTO
     */
    private EmployeePaycheckDto convertToDto(EmployeePayPeriod employeePayPeriod) {
        EmployeePaycheckDto dto = new EmployeePaycheckDto();
        dto.setId(employeePayPeriod.getId());
        dto.setGrossPay(employeePayPeriod.getBaseAmount());
        dto.setBenefitDeductions(employeePayPeriod.getBenefitsAmount());
        dto.setAdditionalDeductions(employeePayPeriod.getAdditionalBenefitCost());
        dto.setNetPay(employeePayPeriod.getTotalAmount());
        dto.setEmployeeFirstName(employeePayPeriod.getEmployee().getFirstName());
        dto.setEmployeeLastName(employeePayPeriod.getEmployee().getLastName());
        dto.setStartDate(employeePayPeriod.getPayrollPeriod().getStartDate());
        dto.setEndDate(employeePayPeriod.getPayrollPeriod().getEndDate());
        return dto;
    }
}