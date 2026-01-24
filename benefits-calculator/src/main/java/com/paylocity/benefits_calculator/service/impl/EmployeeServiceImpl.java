package com.paylocity.benefits_calculator.service.impl;

import com.paylocity.benefits_calculator.dto.EmployeeDto;
import com.paylocity.benefits_calculator.dto.request.CreateEmployeeModel;
import com.paylocity.benefits_calculator.dto.request.PaginationFilter;
import com.paylocity.benefits_calculator.dto.request.UpdateEmployeeModel;
import com.paylocity.benefits_calculator.entity.Employee;
import com.paylocity.benefits_calculator.entity.EmployeePayrate;
import com.paylocity.benefits_calculator.enums.EmployeeStatus;
import com.paylocity.benefits_calculator.repository.EmployeeRepository;
import com.paylocity.benefits_calculator.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of EmployeeService.
 *
 * Handles all employee-related business logic including:
 * - CRUD operations
 * - Entity <-> DTO conversions
 * - Business rule validation
 * - Salary history management
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    /**
     * Create a new employee with initial salary
     */
    @Override
    public EmployeeDto createEmployee(CreateEmployeeModel model) {
        log.info("Creating new employee: {} {}", model.getFirstName(), model.getLastName());

        // Create employee entity
        Employee employee = new Employee();
        employee.setFirstName(model.getFirstName());
        employee.setLastName(model.getLastName());
        employee.setDateOfBirth(model.getDateOfBirth());
        employee.setEmployeeStatus(EmployeeStatus.ACTIVE);

        // Create initial payrate
        EmployeePayrate payrate = new EmployeePayrate();
        payrate.setBaseSalary(model.getSalary());
        payrate.setStartDate(LocalDateTime.now());
        payrate.setEndDate(LocalDateTime.MAX); // Far future date

        // Add payrate to employee
        employee.addPayrate(payrate);

        // Save employee (cascade will save payrate)
        Employee savedEmployee = employeeRepository.save(employee);

        log.info("Employee created successfully with ID: {}", savedEmployee.getId());

        // Convert to DTO
        return convertToDto(savedEmployee);
    }

    /**
     * Get employee by ID
     */
    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        log.info("Fetching employee with ID: {}", id);

        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(id, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.error("Employee not found with ID: {}", id);
                    return new RuntimeException("Employee not found with ID: " + id);
                });

        return convertToDto(employee);
    }

    /**
     * Get all active employees
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        log.info("Fetching all active employees");

        List<Employee> employees = employeeRepository
                .findByEmployeeStatus(EmployeeStatus.ACTIVE);

        return employees.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all active employees with pagination
     */
    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> getAllEmployees(PaginationFilter filter) {
        log.info("Fetching employees with pagination: page {}, size {}",
                filter.getPageNumber(), filter.getPageSize());

        // Create pageable object (Spring Data uses 0-based pages)
        Pageable pageable = PageRequest.of(
                filter.getPageNumber() - 1,  // Convert to 0-based
                filter.getPageSize(),
                Sort.by("lastName", "firstName")  // Sort by last name, then first name
        );

        Page<Employee> employeePage = employeeRepository
                .findAllByEmployeeStatus(EmployeeStatus.ACTIVE, pageable);

        // Convert to DTO page
        return employeePage.map(this::convertToDto);
    }

    /**
     * Update an existing employee
     */
    @Override
    public EmployeeDto updateEmployee(UpdateEmployeeModel model) {
        log.info("Updating employee with ID: {}", model.getId());

        // Find existing employee
        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(model.getId(), EmployeeStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.error("Employee not found with ID: {}", model.getId());
                    return new RuntimeException("Employee not found with ID: " + model.getId());
                });

        // Update basic fields
        employee.setFirstName(model.getFirstName());
        employee.setLastName(model.getLastName());
        employee.setDateOfBirth(model.getDateOfBirth());
        employee.setEmployeeStatus(model.getEmployeeStatus());

        // Check if salary changed
        EmployeePayrate currentPayrate = getCurrentPayrate(employee);
        if (currentPayrate == null ||
                !currentPayrate.getBaseSalary().equals(model.getSalary())) {

            log.info("Salary changed for employee {}, creating new payrate", model.getId());

            // End current payrate
            if (currentPayrate != null) {
                currentPayrate.setEndDate(LocalDateTime.now());
            }

            // Create new payrate
            EmployeePayrate newPayrate = new EmployeePayrate();
            newPayrate.setBaseSalary(model.getSalary());
            newPayrate.setStartDate(LocalDateTime.now());
            newPayrate.setEndDate(LocalDateTime.MAX);
            employee.addPayrate(newPayrate);
        }

        // Save employee
        Employee updatedEmployee = employeeRepository.save(employee);

        log.info("Employee updated successfully: {}", updatedEmployee.getId());

        return convertToDto(updatedEmployee);
    }

    /**
     * Delete an employee (soft delete)
     */
    @Override
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with ID: {}", id);

        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(id, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.error("Employee not found with ID: {}", id);
                    return new RuntimeException("Employee not found with ID: " + id);
                });

        // Soft delete - set status to INACTIVE
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        log.info("Employee deleted successfully: {}", id);
    }

    /**
     * Check if employee exists and is active
     */
    @Override
    @Transactional(readOnly = true)
    public boolean employeeExists(Long id) {
        return employeeRepository.existsByIdAndEmployeeStatus(id, EmployeeStatus.ACTIVE);
    }

    /**
     * Get count of active employees
     */
    @Override
    @Transactional(readOnly = true)
    public long getEmployeeCount() {
        return employeeRepository.countByEmployeeStatus(EmployeeStatus.ACTIVE);
    }

    /**
     * Convert Employee entity to EmployeeDto
     */
    private EmployeeDto convertToDto(Employee employee) {
        EmployeeDto dto = modelMapper.map(employee, EmployeeDto.class);

        // Set current salary
        EmployeePayrate currentPayrate = getCurrentPayrate(employee);
        if (currentPayrate != null) {
            dto.setSalary(currentPayrate.getBaseSalary());
        }

        return dto;
    }

    /**
     * Get current (active) payrate for an employee
     */
    private EmployeePayrate getCurrentPayrate(Employee employee) {
        LocalDateTime now = LocalDateTime.now();

        return employee.getEmployeePayrates().stream()
                .filter(payrate -> payrate.isActiveOn(now))
                .findFirst()
                .orElse(null);
    }
}