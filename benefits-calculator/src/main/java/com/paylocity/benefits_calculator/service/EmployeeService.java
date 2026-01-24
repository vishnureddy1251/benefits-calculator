package com.paylocity.benefits_calculator.service;

import com.paylocity.benefits_calculator.dto.DependentDto;
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
 * - Search functionality
 * - Dependent management
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
     * Create a new employee with dependents
     *
     * @param model the employee data including dependents
     * @return created employee DTO with dependents
     */
    EmployeeDto createEmployeeWithDependents(CreateEmployeeModel model);

    /**
     * Get employee by ID
     *
     * @param id the employee ID
     * @return employee DTO
     */
    EmployeeDto getEmployeeById(Long id);

    /**
     * Get employee by ID with dependents loaded
     *
     * @param id the employee ID
     * @return employee DTO with dependents
     */
    EmployeeDto getEmployeeByIdWithDependents(Long id);

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
     * Search employees by first name
     *
     * @param firstName the first name to search for (case-insensitive)
     * @return list of matching employees
     */
    List<EmployeeDto> searchByFirstName(String firstName);

    /**
     * Search employees by last name
     *
     * @param lastName the last name to search for (case-insensitive)
     * @return list of matching employees
     */
    List<EmployeeDto> searchByLastName(String lastName);

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

    /**
     * Get all dependents for an employee
     *
     * @param employeeId the employee ID
     * @return list of dependent DTOs
     */
    List<DependentDto> getEmployeeDependents(Long employeeId);
}