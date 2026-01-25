package com.paylocity.benefits_calculator.service.impl;

import com.paylocity.benefits_calculator.dto.DependentDto;
import com.paylocity.benefits_calculator.dto.request.CreateDependentModel;
import com.paylocity.benefits_calculator.dto.request.PaginationFilter;
import com.paylocity.benefits_calculator.dto.request.UpdateDependentModel;
import com.paylocity.benefits_calculator.entity.Dependent;
import com.paylocity.benefits_calculator.entity.Employee;
import com.paylocity.benefits_calculator.enums.DependentStatus;
import com.paylocity.benefits_calculator.enums.EmployeeStatus;
import com.paylocity.benefits_calculator.enums.Relationship;
import com.paylocity.benefits_calculator.exception.BusinessValidationException;
import com.paylocity.benefits_calculator.exception.ResourceNotFoundException;
import com.paylocity.benefits_calculator.repository.DependentRepository;
import com.paylocity.benefits_calculator.repository.EmployeeRepository;
import com.paylocity.benefits_calculator.service.DependentService;
import com.paylocity.benefits_calculator.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of DependentService.
 *
 * Handles all dependent-related business logic including:
 * - CRUD operations
 * - Employee relationship validation
 * - Age-based benefit calculations
 * - Spouse/partner rule enforcement
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DependentServiceImpl implements DependentService {

    private final DependentRepository dependentRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    // Inject benefit costs from application.yml
    @Value("${app.payroll.dependent-benefit-cost-monthly-under-50:600.00}")
    private BigDecimal dependentBenefitCostUnder50;

    @Value("${app.payroll.dependent-benefit-cost-monthly-over-50:800.00}")
    private BigDecimal dependentBenefitCostOver50;

    @Override
    public DependentDto createDependent(CreateDependentModel model) {
        log.info("Creating new dependent: {} {} for employee ID: {}",
                model.getFirstName(), model.getLastName(), model.getEmployeeId());

        // Validate dependent age
        ValidationUtil.validateDependentAge(model.getDateOfBirth());

        // Verify employee exists and is active
        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(model.getEmployeeId(), EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", model.getEmployeeId()));

        // Validate spouse/partner rule
        if (isSpouseOrPartner(model.getRelationship())) {
            if (dependentRepository.existsSpouseOrPartnerForEmployee(
                    model.getEmployeeId(), DependentStatus.ACTIVE)) {
                throw new BusinessValidationException(
                        "Employee already has a spouse or domestic partner");
            }
        }

        // Create dependent entity
        Dependent dependent = new Dependent();
        dependent.setFirstName(model.getFirstName());
        dependent.setLastName(model.getLastName());
        dependent.setDateOfBirth(model.getDateOfBirth());
        dependent.setRelationship(model.getRelationship());
        dependent.setGender(model.getGender());
        dependent.setDependentStatus(DependentStatus.ACTIVE);
        dependent.setEmployee(employee);

        // Save dependent
        Dependent savedDependent = dependentRepository.save(dependent);

        log.info("Dependent created successfully with ID: {}", savedDependent.getId());

        return convertToDto(savedDependent);
    }

    @Override
    @Transactional(readOnly = true)
    public DependentDto getDependentById(Long id) {
        log.info("Fetching dependent with ID: {}", id);

        Dependent dependent = dependentRepository
                .findByIdAndDependentStatus(id, DependentStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Dependent", "id", id));

        return convertToDto(dependent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DependentDto> getAllDependents() {
        log.info("Fetching all active dependents");

        List<Dependent> dependents = dependentRepository
                .findByDependentStatus(DependentStatus.ACTIVE);

        return dependents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DependentDto> getAllDependents(PaginationFilter filter) {
        log.info("Fetching dependents with pagination: page {}, size {}",
                filter.getPageNumber(), filter.getPageSize());

        Pageable pageable = PageRequest.of(
                filter.getPageNumber() - 1,
                filter.getPageSize(),
                Sort.by("lastName", "firstName")
        );

        Page<Dependent> dependentPage = dependentRepository
                .findAllByDependentStatus(DependentStatus.ACTIVE, pageable);

        return dependentPage.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DependentDto> getDependentsByEmployeeId(Long employeeId) {
        log.info("Fetching dependents for employee ID: {}", employeeId);

        // Verify employee exists
        if (!employeeRepository.existsByIdAndEmployeeStatus(employeeId, EmployeeStatus.ACTIVE)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        List<Dependent> dependents = dependentRepository
                .findByEmployee_IdAndDependentStatus(employeeId, DependentStatus.ACTIVE);

        log.info("Found {} dependents for employee {}", dependents.size(), employeeId);

        return dependents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DependentDto> getDependentsByEmployeeId(Long employeeId, PaginationFilter filter) {
        log.info("Fetching dependents for employee {} with pagination", employeeId);

        // Verify employee exists
        if (!employeeRepository.existsByIdAndEmployeeStatus(employeeId, EmployeeStatus.ACTIVE)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        Pageable pageable = PageRequest.of(
                filter.getPageNumber() - 1,
                filter.getPageSize(),
                Sort.by("relationship", "lastName")
        );

        Page<Dependent> dependentPage = dependentRepository
                .findByEmployee_IdAndDependentStatus(employeeId, DependentStatus.ACTIVE, pageable);

        return dependentPage.map(this::convertToDto);
    }

    @Override
    public DependentDto updateDependent(UpdateDependentModel model) {
        log.info("Updating dependent with ID: {}", model.getId());

        // Validate dependent age
        ValidationUtil.validateDependentAge(model.getDateOfBirth());

        // Find existing dependent
        Dependent dependent = dependentRepository
                .findByIdAndDependentStatus(model.getId(), DependentStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Dependent", "id", model.getId()));

        // Check if employee is changing
        if (!dependent.getEmployee().getId().equals(model.getEmployeeId())) {
            // Verify new employee exists
            Employee newEmployee = employeeRepository
                    .findByIdAndEmployeeStatus(model.getEmployeeId(), EmployeeStatus.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", model.getEmployeeId()));

            dependent.setEmployee(newEmployee);
        }

        // Check if relationship is changing to spouse/partner
        if (isSpouseOrPartner(model.getRelationship()) &&
                !isSpouseOrPartner(dependent.getRelationship())) {

            // Validate spouse/partner rule for the employee
            if (dependentRepository.existsSpouseOrPartnerForEmployee(
                    model.getEmployeeId(), DependentStatus.ACTIVE)) {
                throw new BusinessValidationException(
                        "Employee already has a spouse or domestic partner");
            }
        }

        // Update fields
        dependent.setFirstName(model.getFirstName());
        dependent.setLastName(model.getLastName());
        dependent.setDateOfBirth(model.getDateOfBirth());
        dependent.setRelationship(model.getRelationship());
        dependent.setGender(model.getGender());
        dependent.setDependentStatus(model.getDependentStatus());

        // Save dependent
        Dependent updatedDependent = dependentRepository.save(dependent);

        log.info("Dependent updated successfully: {}", updatedDependent.getId());

        return convertToDto(updatedDependent);
    }

    @Override
    public void deleteDependent(Long id) {
        log.info("Deleting dependent with ID: {}", id);

        Dependent dependent = dependentRepository
                .findByIdAndDependentStatus(id, DependentStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Dependent", "id", id));

        // Soft delete
        dependent.setDependentStatus(DependentStatus.INACTIVE);
        dependentRepository.save(dependent);

        log.info("Dependent deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean dependentExists(Long id) {
        return dependentRepository.existsByIdAndDependentStatus(id, DependentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public long getDependentCount(Long employeeId) {
        return dependentRepository.countByEmployee_IdAndDependentStatus(
                employeeId, DependentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalDependentCount() {
        return dependentRepository.countByDependentStatus(DependentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSpouseOrPartner(Long employeeId) {
        return dependentRepository.existsSpouseOrPartnerForEmployee(
                employeeId, DependentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateMonthlyBenefitCost(Long dependentId) {
        log.info("Calculating monthly benefit cost for dependent ID: {}", dependentId);

        Dependent dependent = dependentRepository
                .findByIdAndDependentStatus(dependentId, DependentStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Dependent", "id", dependentId));

        // Check if dependent is over 50
        if (dependent.isOver50()) {
            log.debug("Dependent {} is over 50, cost: {}", dependentId, dependentBenefitCostOver50);
            return dependentBenefitCostOver50;
        } else {
            log.debug("Dependent {} is under 50, cost: {}", dependentId, dependentBenefitCostUnder50);
            return dependentBenefitCostUnder50;
        }
    }

    /**
     * Check if relationship is spouse or domestic partner
     */
    private boolean isSpouseOrPartner(Relationship relationship) {
        return relationship == Relationship.SPOUSE ||
                relationship == Relationship.DOMESTIC_PARTNER;
    }

    /**
     * Convert Dependent entity to DTO
     */
    private DependentDto convertToDto(Dependent dependent) {
        DependentDto dto = modelMapper.map(dependent, DependentDto.class);
        dto.setEmployeeId(dependent.getEmployee().getId());
        return dto;
    }
}