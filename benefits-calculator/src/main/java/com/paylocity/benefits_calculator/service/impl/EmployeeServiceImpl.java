package com.paylocity.benefits_calculator.service.impl;

import com.paylocity.benefits_calculator.dto.DependentDto;
import com.paylocity.benefits_calculator.dto.EmployeeDto;
import com.paylocity.benefits_calculator.dto.request.CreateDependentModel;
import com.paylocity.benefits_calculator.dto.request.CreateEmployeeModel;
import com.paylocity.benefits_calculator.dto.request.PaginationFilter;
import com.paylocity.benefits_calculator.dto.request.UpdateEmployeeModel;
import com.paylocity.benefits_calculator.entity.Dependent;
import com.paylocity.benefits_calculator.entity.Employee;
import com.paylocity.benefits_calculator.entity.EmployeePayrate;
import com.paylocity.benefits_calculator.enums.DependentStatus;
import com.paylocity.benefits_calculator.enums.EmployeeStatus;
import com.paylocity.benefits_calculator.exception.BusinessValidationException;
import com.paylocity.benefits_calculator.exception.ResourceNotFoundException;
import com.paylocity.benefits_calculator.repository.DependentRepository;
import com.paylocity.benefits_calculator.repository.EmployeeRepository;
import com.paylocity.benefits_calculator.service.EmployeeService;
import com.paylocity.benefits_calculator.util.ValidationUtil;
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
 * Enhanced implementation of EmployeeService with advanced features.
 *
 * @author Benefits Calculator Team
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DependentRepository dependentRepository;
    private final ModelMapper modelMapper;

    @Override
    public EmployeeDto createEmployee(CreateEmployeeModel model) {
        log.info("Creating new employee: {} {}", model.getFirstName(), model.getLastName());

        // Validate business rules
        ValidationUtil.validateEmployeeAge(model.getDateOfBirth());
        ValidationUtil.validateSalary(model.getSalary());

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
        payrate.setEndDate(LocalDateTime.MAX);

        // Add payrate to employee
        employee.addPayrate(payrate);

        // Save employee
        Employee savedEmployee = employeeRepository.save(employee);

        log.info("Employee created successfully with ID: {}", savedEmployee.getId());

        return convertToDto(savedEmployee);
    }

    @Override
    public EmployeeDto createEmployeeWithDependents(CreateEmployeeModel model) {
        log.info("Creating new employee with {} dependents: {} {}",
                model.getDependents() != null ? model.getDependents().size() : 0,
                model.getFirstName(), model.getLastName());

        // Validate business rules
        ValidationUtil.validateEmployeeAge(model.getDateOfBirth());
        ValidationUtil.validateSalary(model.getSalary());

        // Create employee
        Employee employee = new Employee();
        employee.setFirstName(model.getFirstName());
        employee.setLastName(model.getLastName());
        employee.setDateOfBirth(model.getDateOfBirth());
        employee.setEmployeeStatus(EmployeeStatus.ACTIVE);

        // Create initial payrate
        EmployeePayrate payrate = new EmployeePayrate();
        payrate.setBaseSalary(model.getSalary());
        payrate.setStartDate(LocalDateTime.now());
        payrate.setEndDate(LocalDateTime.MAX);
        employee.addPayrate(payrate);

        // Add dependents if provided
        if (model.getDependents() != null && !model.getDependents().isEmpty()) {
            validateDependents(model.getDependents());

            for (CreateDependentModel depModel : model.getDependents()) {
                Dependent dependent = createDependentFromModel(depModel, employee);
                employee.addDependent(dependent);
            }
        }

        // Save employee (cascade will save dependents)
        Employee savedEmployee = employeeRepository.save(employee);

        log.info("Employee created with {} dependents, ID: {}",
                savedEmployee.getDependents().size(), savedEmployee.getId());

        return convertToDtoWithDependents(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        log.info("Fetching employee with ID: {}", id);

        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(id, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        return convertToDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeByIdWithDependents(Long id) {
        log.info("Fetching employee with dependents, ID: {}", id);

        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(id, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        return convertToDtoWithDependents(employee);
    }

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

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> getAllEmployees(PaginationFilter filter) {
        log.info("Fetching employees with pagination: page {}, size {}",
                filter.getPageNumber(), filter.getPageSize());

        Pageable pageable = PageRequest.of(
                filter.getPageNumber() - 1,
                filter.getPageSize(),
                Sort.by("lastName", "firstName")
        );

        Page<Employee> employeePage = employeeRepository
                .findAllByEmployeeStatus(EmployeeStatus.ACTIVE, pageable);

        return employeePage.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> searchByFirstName(String firstName) {
        log.info("Searching employees by first name: {}", firstName);

        if (firstName == null || firstName.trim().isEmpty()) {
            throw new BusinessValidationException("First name search term cannot be empty");
        }

        List<Employee> employees = employeeRepository
                .findByFirstNameContainingIgnoreCaseAndEmployeeStatus(
                        firstName.trim(), EmployeeStatus.ACTIVE);

        log.info("Found {} employees matching first name: {}", employees.size(), firstName);

        return employees.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> searchByLastName(String lastName) {
        log.info("Searching employees by last name: {}", lastName);

        if (lastName == null || lastName.trim().isEmpty()) {
            throw new BusinessValidationException("Last name search term cannot be empty");
        }

        List<Employee> employees = employeeRepository
                .findByLastNameContainingIgnoreCaseAndEmployeeStatus(
                        lastName.trim(), EmployeeStatus.ACTIVE);

        log.info("Found {} employees matching last name: {}", employees.size(), lastName);

        return employees.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDto updateEmployee(UpdateEmployeeModel model) {
        log.info("Updating employee with ID: {}", model.getId());

        // Validate business rules
        ValidationUtil.validateEmployeeAge(model.getDateOfBirth());
        ValidationUtil.validateSalary(model.getSalary());

        // Find existing employee
        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(model.getId(), EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", model.getId()));

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

            if (currentPayrate != null) {
                currentPayrate.setEndDate(LocalDateTime.now());
            }

            EmployeePayrate newPayrate = new EmployeePayrate();
            newPayrate.setBaseSalary(model.getSalary());
            newPayrate.setStartDate(LocalDateTime.now());
            newPayrate.setEndDate(LocalDateTime.MAX);
            employee.addPayrate(newPayrate);
        }

        Employee updatedEmployee = employeeRepository.save(employee);

        log.info("Employee updated successfully: {}", updatedEmployee.getId());

        return convertToDto(updatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with ID: {}", id);

        Employee employee = employeeRepository
                .findByIdAndEmployeeStatus(id, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        log.info("Employee deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean employeeExists(Long id) {
        return employeeRepository.existsByIdAndEmployeeStatus(id, EmployeeStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public long getEmployeeCount() {
        return employeeRepository.countByEmployeeStatus(EmployeeStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DependentDto> getEmployeeDependents(Long employeeId) {
        log.info("Fetching dependents for employee ID: {}", employeeId);

        // Verify employee exists
        if (!employeeExists(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        List<Dependent> dependents = dependentRepository
                .findByEmployee_IdAndDependentStatus(employeeId, DependentStatus.ACTIVE);

        log.info("Found {} dependents for employee {}", dependents.size(), employeeId);

        return dependents.stream()
                .map(dep -> modelMapper.map(dep, DependentDto.class))
                .collect(Collectors.toList());
    }

    /**
     * Validate list of dependents
     */
    private void validateDependents(List<CreateDependentModel> dependents) {
        if (dependents == null || dependents.isEmpty()) {
            return;
        }

        // Validate each dependent
        for (CreateDependentModel dep : dependents) {
            ValidationUtil.validateDependentAge(dep.getDateOfBirth());
        }

        // Check for multiple spouse/partners (business rule)
        long spouseCount = dependents.stream()
                .filter(dep -> dep.getRelationship() != null)
                .filter(dep -> dep.getRelationship().name().equals("SPOUSE") ||
                        dep.getRelationship().name().equals("DOMESTIC_PARTNER"))
                .count();

        if (spouseCount > 1) {
            throw new BusinessValidationException(
                    "Employee can have only one spouse or domestic partner");
        }
    }

    /**
     * Create Dependent entity from model
     */
    private Dependent createDependentFromModel(CreateDependentModel model, Employee employee) {
        Dependent dependent = new Dependent();
        dependent.setFirstName(model.getFirstName());
        dependent.setLastName(model.getLastName());
        dependent.setDateOfBirth(model.getDateOfBirth());
        dependent.setRelationship(model.getRelationship());
        dependent.setGender(model.getGender());
        dependent.setDependentStatus(DependentStatus.ACTIVE);
        dependent.setEmployee(employee);
        return dependent;
    }

    /**
     * Convert Employee to DTO
     */
    private EmployeeDto convertToDto(Employee employee) {
        EmployeeDto dto = modelMapper.map(employee, EmployeeDto.class);

        EmployeePayrate currentPayrate = getCurrentPayrate(employee);
        if (currentPayrate != null) {
            dto.setSalary(currentPayrate.getBaseSalary());
        }

        return dto;
    }

    /**
     * Convert Employee to DTO with dependents
     */
    private EmployeeDto convertToDtoWithDependents(Employee employee) {
        EmployeeDto dto = convertToDto(employee);

        // Add dependents
        List<DependentDto> dependentDtos = employee.getDependents().stream()
                .filter(dep -> dep.getDependentStatus() == DependentStatus.ACTIVE)
                .map(dep -> modelMapper.map(dep, DependentDto.class))
                .collect(Collectors.toList());

        dto.setDependents(dependentDtos);

        return dto;
    }

    /**
     * Get current payrate for employee
     */
    private EmployeePayrate getCurrentPayrate(Employee employee) {
        LocalDateTime now = LocalDateTime.now();

        return employee.getEmployeePayrates().stream()
                .filter(payrate -> payrate.isActiveOn(now))
                .findFirst()
                .orElse(null);
    }
}