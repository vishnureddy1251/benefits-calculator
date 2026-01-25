package com.paylocity.benefits_calculator.service;

import com.paylocity.benefits_calculator.dto.DependentDto;
import com.paylocity.benefits_calculator.dto.request.CreateDependentModel;
import com.paylocity.benefits_calculator.dto.request.PaginationFilter;
import com.paylocity.benefits_calculator.dto.request.UpdateDependentModel;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for Dependent operations.
 *
 * Defines the contract for dependent business logic including:
 * - CRUD operations
 * - Employee relationship management
 * - Business rule validation
 * - Age-based benefit calculations
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
public interface DependentService {

    /**
     * Create a new dependent for an employee
     *
     * @param model the dependent data
     * @return created dependent DTO
     */
    DependentDto createDependent(CreateDependentModel model);

    /**
     * Get dependent by ID
     *
     * @param id the dependent ID
     * @return dependent DTO
     */
    DependentDto getDependentById(Long id);

    /**
     * Get all active dependents
     *
     * @return list of dependent DTOs
     */
    List<DependentDto> getAllDependents();

    /**
     * Get all active dependents with pagination
     *
     * @param filter pagination parameters
     * @return page of dependent DTOs
     */
    Page<DependentDto> getAllDependents(PaginationFilter filter);

    /**
     * Get all dependents for a specific employee
     *
     * @param employeeId the employee ID
     * @return list of dependent DTOs
     */
    List<DependentDto> getDependentsByEmployeeId(Long employeeId);

    /**
     * Get all dependents for a specific employee with pagination
     *
     * @param employeeId the employee ID
     * @param filter pagination parameters
     * @return page of dependent DTOs
     */
    Page<DependentDto> getDependentsByEmployeeId(Long employeeId, PaginationFilter filter);

    /**
     * Update an existing dependent
     *
     * @param model the updated dependent data
     * @return updated dependent DTO
     */
    DependentDto updateDependent(UpdateDependentModel model);

    /**
     * Delete a dependent (soft delete)
     *
     * @param id the dependent ID
     */
    void deleteDependent(Long id);

    /**
     * Check if a dependent exists and is active
     *
     * @param id the dependent ID
     * @return true if dependent exists and is active
     */
    boolean dependentExists(Long id);

    /**
     * Get count of active dependents for an employee
     *
     * @param employeeId the employee ID
     * @return count of active dependents
     */
    long getDependentCount(Long employeeId);

    /**
     * Get count of all active dependents
     *
     * @return total count of active dependents
     */
    long getTotalDependentCount();

    /**
     * Check if employee has a spouse or domestic partner
     *
     * @param employeeId the employee ID
     * @return true if employee has spouse or domestic partner
     */
    boolean hasSpouseOrPartner(Long employeeId);

    /**
     * Calculate monthly benefit cost for a dependent
     *
     * @param dependentId the dependent ID
     * @return monthly benefit cost
     */
    java.math.BigDecimal calculateMonthlyBenefitCost(Long dependentId);
}