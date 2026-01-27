package com.paylocity.benefits_calculator.service;

import com.paylocity.benefits_calculator.dto.EmployeeDto;
import com.paylocity.benefits_calculator.dto.request.CreateEmployeeModel;
import com.paylocity.benefits_calculator.dto.request.UpdateEmployeeModel;
import com.paylocity.benefits_calculator.entity.Employee;
import com.paylocity.benefits_calculator.enums.EmployeeStatus;
import com.paylocity.benefits_calculator.exception.BusinessValidationException;
import com.paylocity.benefits_calculator.exception.ResourceNotFoundException;
import com.paylocity.benefits_calculator.repository.EmployeeRepository;
import com.paylocity.benefits_calculator.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService.
 *
 * Tests service layer business logic using mocked dependencies.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee testEmployee;
    private CreateEmployeeModel createModel;
    private EmployeeDto employeeDto;

    @BeforeEach
    void setUp() {
        // Set up test data
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDateOfBirth(LocalDate.of(1985, 5, 15));
        testEmployee.setEmployeeStatus(EmployeeStatus.ACTIVE);
        testEmployee.setEmployeePayrates(new ArrayList<>());
        testEmployee.setDependents(new ArrayList<>());

        createModel = new CreateEmployeeModel();
        createModel.setFirstName("John");
        createModel.setLastName("Doe");
        createModel.setDateOfBirth(LocalDate.of(1985, 5, 15));
        createModel.setSalary(new BigDecimal("100000.00"));

        employeeDto = new EmployeeDto();
        employeeDto.setId(1L);
        employeeDto.setFirstName("John");
        employeeDto.setLastName("Doe");
        employeeDto.setDateOfBirth(LocalDate.of(1985, 5, 15));
        employeeDto.setSalary(new BigDecimal("100000.00"));
    }

    @Test
    @DisplayName("Should create employee successfully")
    void testCreateEmployee_Success() {
        // Arrange
        when(modelMapper.map(any(CreateEmployeeModel.class), eq(Employee.class)))
                .thenReturn(testEmployee);
        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(testEmployee);
        when(modelMapper.map(any(Employee.class), eq(EmployeeDto.class)))
                .thenReturn(employeeDto);

        // Act
        EmployeeDto result = employeeService.createEmployee(createModel);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when creating employee under 18")
    void testCreateEmployee_UnderAge() {
        // Arrange
        createModel.setDateOfBirth(LocalDate.now().minusYears(17));

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> {
            employeeService.createEmployee(createModel);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when creating employee over 100")
    void testCreateEmployee_OverAge() {
        // Arrange
        createModel.setDateOfBirth(LocalDate.now().minusYears(101));

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> {
            employeeService.createEmployee(createModel);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when salary is zero")
    void testCreateEmployee_ZeroSalary() {
        // Arrange
        createModel.setSalary(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> {
            employeeService.createEmployee(createModel);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when salary is negative")
    void testCreateEmployee_NegativeSalary() {
        // Arrange
        createModel.setSalary(new BigDecimal("-1000"));

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> {
            employeeService.createEmployee(createModel);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw exception when salary exceeds maximum")
    void testCreateEmployee_SalaryTooHigh() {
        // Arrange
        createModel.setSalary(new BigDecimal("20000000"));

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> {
            employeeService.createEmployee(createModel);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should get employee by ID successfully")
    void testGetEmployeeById_Success() {
        // Arrange
        when(employeeRepository.findByIdAndEmployeeStatus(1L, EmployeeStatus.ACTIVE))
                .thenReturn(Optional.of(testEmployee));
        when(modelMapper.map(any(Employee.class), eq(EmployeeDto.class)))
                .thenReturn(employeeDto);

        // Act
        EmployeeDto result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        verify(employeeRepository, times(1)).findByIdAndEmployeeStatus(1L, EmployeeStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should throw exception when employee not found")
    void testGetEmployeeById_NotFound() {
        // Arrange
        when(employeeRepository.findByIdAndEmployeeStatus(999L, EmployeeStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });
    }

    @Test
    @DisplayName("Should get all employees")
    void testGetAllEmployees() {
        // Arrange
        List<Employee> employees = List.of(testEmployee);
        when(employeeRepository.findByEmployeeStatus(EmployeeStatus.ACTIVE))
                .thenReturn(employees);
        when(modelMapper.map(any(Employee.class), eq(EmployeeDto.class)))
                .thenReturn(employeeDto);

        // Act
        List<EmployeeDto> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).findByEmployeeStatus(EmployeeStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should search employees by first name")
    void testSearchByFirstName() {
        // Arrange
        List<Employee> employees = List.of(testEmployee);
        when(employeeRepository.findByFirstNameContainingIgnoreCaseAndEmployeeStatus(
                "john", EmployeeStatus.ACTIVE))
                .thenReturn(employees);
        when(modelMapper.map(any(Employee.class), eq(EmployeeDto.class)))
                .thenReturn(employeeDto);

        // Act
        List<EmployeeDto> result = employeeService.searchByFirstName("john");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
    }

    @Test
    @DisplayName("Should search employees by last name")
    void testSearchByLastName() {
        // Arrange
        List<Employee> employees = List.of(testEmployee);
        when(employeeRepository.findByLastNameContainingIgnoreCaseAndEmployeeStatus(
                "doe", EmployeeStatus.ACTIVE))
                .thenReturn(employees);
        when(modelMapper.map(any(Employee.class), eq(EmployeeDto.class)))
                .thenReturn(employeeDto);

        // Act
        List<EmployeeDto> result = employeeService.searchByLastName("doe");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Doe", result.get(0).getLastName());
    }

    @Test
    @DisplayName("Should update employee successfully")
    void testUpdateEmployee_Success() {
        // Arrange
        UpdateEmployeeModel updateModel = new UpdateEmployeeModel();
        updateModel.setId(1L);
        updateModel.setFirstName("John");
        updateModel.setLastName("Doe");
        updateModel.setDateOfBirth(LocalDate.of(1985, 5, 15));
        updateModel.setEmployeeStatus(EmployeeStatus.ACTIVE);
        updateModel.setSalary(new BigDecimal("110000.00"));

        when(employeeRepository.findByIdAndEmployeeStatus(1L, EmployeeStatus.ACTIVE))
                .thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(testEmployee);
        when(modelMapper.map(any(Employee.class), eq(EmployeeDto.class)))
                .thenReturn(employeeDto);

        // Act
        EmployeeDto result = employeeService.updateEmployee(updateModel);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should soft delete employee")
    void testDeleteEmployee() {
        // Arrange
        when(employeeRepository.findByIdAndEmployeeStatus(1L, EmployeeStatus.ACTIVE))
                .thenReturn(Optional.of(testEmployee));

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        assertEquals(EmployeeStatus.INACTIVE, testEmployee.getEmployeeStatus());
        verify(employeeRepository, times(1)).save(testEmployee);
    }

    @Test
    @DisplayName("Should check if employee exists")
    void testEmployeeExists() {
        // Arrange
        when(employeeRepository.existsByIdAndEmployeeStatus(1L, EmployeeStatus.ACTIVE))
                .thenReturn(true);

        // Act
        boolean result = employeeService.employeeExists(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should get employee count")
    void testGetEmployeeCount() {
        // Arrange
        when(employeeRepository.countByEmployeeStatus(EmployeeStatus.ACTIVE))
                .thenReturn(5L);

        // Act
        long result = employeeService.getEmployeeCount();

        // Assert
        assertEquals(5L, result);
    }
}