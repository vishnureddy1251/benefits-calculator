package com.paylocity.benefits_calculator.service;

import com.paylocity.benefits_calculator.dto.EmployeeDto;
import com.paylocity.benefits_calculator.dto.request.CreateEmployeeModel;
import com.paylocity.benefits_calculator.dto.request.PaginationFilter;
import com.paylocity.benefits_calculator.dto.request.UpdateEmployeeModel;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for Employee operations.
 *
 * Defines the contract for employee business logic including:
 * - CRUD operations
 * - Pagination support
 * - Business rule validation
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
public interface EmployeeService {

    /**
     * Create a new employee
     *
     * @param model the employee data
     * @return created employee DTO
     */
    EmployeeDto createEmployee(CreateEmployeeModel model);

    /**
     * Get employee by ID
     *
     * @param id the employee ID
     * @return employee DTO
     */
    EmployeeDto getEmployeeById(Long id);

    /**
     * Get all active employees
     *
     * @return list of employee DTOs
     */
    List<EmployeeDto> getAllEmployees();

    /**
     * Get all active employees with pagination
     *
     * @param filter pagination parameters
     * @return page of employee DTOs
     */
    Page<EmployeeDto> getAllEmployees(PaginationFilter filter);

    /**
     * Update an existing employee
     *
     * @param model the updated employee data
     * @return updated employee DTO
     */
    EmployeeDto updateEmployee(UpdateEmployeeModel model);

    /**
     * Delete an employee (soft delete)
     *
     * @param id the employee ID
     */
    void deleteEmployee(Long id);

    /**
     * Check if an employee exists and is active
     *
     * @param id the employee ID
     * @return true if employee exists and is active
     */
    boolean employeeExists(Long id);

    /**
     * Get total count of active employees
     *
     * @return count of active employees
     */
    long getEmployeeCount();
}