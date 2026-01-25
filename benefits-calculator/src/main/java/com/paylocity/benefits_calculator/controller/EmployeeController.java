package com.paylocity.benefits_calculator.controller;

import com.paylocity.benefits_calculator.dto.ApiResponse;
import com.paylocity.benefits_calculator.dto.DependentDto;
import com.paylocity.benefits_calculator.dto.EmployeeDto;
import com.paylocity.benefits_calculator.dto.request.CreateEmployeeModel;
import com.paylocity.benefits_calculator.dto.request.PaginationFilter;
import com.paylocity.benefits_calculator.dto.request.UpdateEmployeeModel;
import com.paylocity.benefits_calculator.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Employee operations.
 *
 * Provides endpoints for:
 * - CRUD operations on employees
 * - Search functionality
 * - Pagination support
 * - Dependent management
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Create a new employee
     *
     * POST /api/v1/employees
     */
    @PostMapping
    @Operation(summary = "Create employee", description = "Create a new employee with optional dependents")
    public ResponseEntity<ApiResponse<EmployeeDto>> createEmployee(
            @Valid @RequestBody CreateEmployeeModel model) {

        log.info("Creating employee: {} {}", model.getFirstName(), model.getLastName());

        EmployeeDto employee;

        if (model.getDependents() != null && !model.getDependents().isEmpty()) {
            employee = employeeService.createEmployeeWithDependents(model);
            log.info("Employee created with {} dependents", model.getDependents().size());
        } else {
            employee = employeeService.createEmployee(model);
            log.info("Employee created without dependents");
        }

        ApiResponse<EmployeeDto> response = ApiResponse.success(
                employee,
                "Employee created successfully"
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get employee by ID
     *
     * GET /api/v1/employees/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Retrieve a single employee by their ID")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Parameter(description = "Include dependents") @RequestParam(defaultValue = "false") boolean includeDependents) {

        log.info("Fetching employee with ID: {}, includeDependents: {}", id, includeDependents);

        EmployeeDto employee = includeDependents
                ? employeeService.getEmployeeByIdWithDependents(id)
                : employeeService.getEmployeeById(id);

        ApiResponse<EmployeeDto> response = ApiResponse.success(employee);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all employees
     *
     * GET /api/v1/employees
     */
    @GetMapping
    @Operation(summary = "Get all employees", description = "Retrieve all active employees with optional pagination")
    public ResponseEntity<ApiResponse<?>> getAllEmployees(
            @Parameter(description = "Page number (1-based)") @RequestParam(required = false) Integer pageNumber,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer pageSize) {

        if (pageNumber != null && pageSize != null) {
            log.info("Fetching employees with pagination: page {}, size {}", pageNumber, pageSize);

            PaginationFilter filter = new PaginationFilter(pageNumber, pageSize);
            Page<EmployeeDto> employeePage = employeeService.getAllEmployees(filter);

            ApiResponse<Page<EmployeeDto>> response = ApiResponse.success(
                    employeePage,
                    String.format("Retrieved %d employees (page %d of %d)",
                            employeePage.getNumberOfElements(),
                            employeePage.getNumber() + 1,
                            employeePage.getTotalPages())
            );

            return ResponseEntity.ok(response);
        } else {
            log.info("Fetching all employees without pagination");

            List<EmployeeDto> employees = employeeService.getAllEmployees();

            ApiResponse<List<EmployeeDto>> response = ApiResponse.success(
                    employees,
                    String.format("Retrieved %d employees", employees.size())
            );

            return ResponseEntity.ok(response);
        }
    }

    /**
     * Search employees by first name
     *
     * GET /api/v1/employees/search/firstName?q=John
     */
    @GetMapping("/search/firstName")
    @Operation(summary = "Search by first name", description = "Search employees by first name (case-insensitive, partial match)")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> searchByFirstName(
            @Parameter(description = "Search term") @RequestParam String q) {

        log.info("Searching employees by first name: {}", q);

        List<EmployeeDto> employees = employeeService.searchByFirstName(q);

        ApiResponse<List<EmployeeDto>> response = ApiResponse.success(
                employees,
                String.format("Found %d employees matching '%s'", employees.size(), q)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Search employees by last name
     *
     * GET /api/v1/employees/search/lastName?q=Doe
     */
    @GetMapping("/search/lastName")
    @Operation(summary = "Search by last name", description = "Search employees by last name (case-insensitive, partial match)")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> searchByLastName(
            @Parameter(description = "Search term") @RequestParam String q) {

        log.info("Searching employees by last name: {}", q);

        List<EmployeeDto> employees = employeeService.searchByLastName(q);

        ApiResponse<List<EmployeeDto>> response = ApiResponse.success(
                employees,
                String.format("Found %d employees matching '%s'", employees.size(), q)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update employee
     *
     * PUT /api/v1/employees/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Update an existing employee")
    public ResponseEntity<ApiResponse<EmployeeDto>> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeModel model) {

        log.info("Updating employee with ID: {}", id);

        // Ensure ID in path matches ID in body
        model.setId(id);

        EmployeeDto employee = employeeService.updateEmployee(model);

        ApiResponse<EmployeeDto> response = ApiResponse.success(
                employee,
                "Employee updated successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Delete employee
     *
     * DELETE /api/v1/employees/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee", description = "Soft delete an employee (sets status to INACTIVE)")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {

        log.info("Deleting employee with ID: {}", id);

        employeeService.deleteEmployee(id);

        ApiResponse<Void> response = ApiResponse.success(
                null,
                "Employee deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get employee's dependents
     *
     * GET /api/v1/employees/{id}/dependents
     */
    @GetMapping("/{id}/dependents")
    @Operation(summary = "Get employee's dependents", description = "Retrieve all dependents for a specific employee")
    public ResponseEntity<ApiResponse<List<DependentDto>>> getEmployeeDependents(
            @Parameter(description = "Employee ID") @PathVariable Long id) {

        log.info("Fetching dependents for employee ID: {}", id);

        List<DependentDto> dependents = employeeService.getEmployeeDependents(id);

        ApiResponse<List<DependentDto>> response = ApiResponse.success(
                dependents,
                String.format("Employee has %d dependent(s)", dependents.size())
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get employee count
     *
     * GET /api/v1/employees/count
     */
    @GetMapping("/count")
    @Operation(summary = "Get employee count", description = "Get total count of active employees")
    public ResponseEntity<ApiResponse<Long>> getEmployeeCount() {

        log.info("Fetching employee count");

        long count = employeeService.getEmployeeCount();

        ApiResponse<Long> response = ApiResponse.success(
                count,
                String.format("Total active employees: %d", count)
        );

        return ResponseEntity.ok(response);
    }
}