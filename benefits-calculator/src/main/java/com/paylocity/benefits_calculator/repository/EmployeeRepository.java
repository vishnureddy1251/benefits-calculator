package com.paylocity.benefits_calculator.repository;

import com.paylocity.benefits_calculator.entity.Employee;
import com.paylocity.benefits_calculator.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity.
 *
 * Provides database operations for Employee management including:
 * - CRUD operations (inherited from JpaRepository)
 * - Custom queries for finding active employees
 * - Pagination support
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find an active employee by ID with dependents and payrates eagerly loaded.
     *
     * @param id the employee ID
     * @return Optional containing the employee if found and active
     */
    @Query("SELECT e FROM Employee e " +
            "LEFT JOIN FETCH e.dependents " +
            "LEFT JOIN FETCH e.employeePayrates " +
            "WHERE e.id = :id AND e.employeeStatus = :status")
    Optional<Employee> findByIdAndEmployeeStatus(
            @Param("id") Long id,
            @Param("status") EmployeeStatus status
    );

    /**
     * Find all active employees with pagination.
     * Uses entity graph to avoid N+1 query problem.
     *
     * @param status the employee status to filter by
     * @param pageable pagination information
     * @return page of employees
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
            "LEFT JOIN FETCH e.dependents " +
            "LEFT JOIN FETCH e.employeePayrates " +
            "WHERE e.employeeStatus = :status")
    Page<Employee> findAllByEmployeeStatus(
            @Param("status") EmployeeStatus status,
            Pageable pageable
    );

    /**
     * Find all employees by status (without pagination).
     *
     * @param status the employee status to filter by
     * @return list of employees
     */
    List<Employee> findByEmployeeStatus(EmployeeStatus status);

    /**
     * Find all active employees by IDs.
     * Useful for batch operations.
     *
     * @param ids list of employee IDs
     * @param status the employee status to filter by
     * @return list of employees
     */
    List<Employee> findByIdInAndEmployeeStatus(
            List<Long> ids,
            EmployeeStatus status
    );

    /**
     * Check if an employee exists and is active.
     *
     * @param id the employee ID
     * @param status the employee status to check
     * @return true if employee exists and has the given status
     */
    boolean existsByIdAndEmployeeStatus(Long id, EmployeeStatus status);

    /**
     * Count active employees.
     *
     * @param status the employee status to count
     * @return number of employees with the given status
     */
    long countByEmployeeStatus(EmployeeStatus status);

    /**
     * Find employees by first name (case-insensitive) and status.
     *
     * @param firstName the first name to search for
     * @param status the employee status to filter by
     * @return list of matching employees
     */
    List<Employee> findByFirstNameContainingIgnoreCaseAndEmployeeStatus(
            String firstName,
            EmployeeStatus status
    );

    /**
     * Find employees by last name (case-insensitive) and status.
     *
     * @param lastName the last name to search for
     * @param status the employee status to filter by
     * @return list of matching employees
     */
    List<Employee> findByLastNameContainingIgnoreCaseAndEmployeeStatus(
            String lastName,
            EmployeeStatus status
    );
}