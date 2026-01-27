package com.paylocity.benefits_calculator.service;

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
import com.paylocity.benefits_calculator.service.impl.PayrollServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payroll Service Tests")
class PayrollServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DependentRepository dependentRepository;

    @Mock
    private PayrollPeriodRepository payrollPeriodRepository;

    @Mock
    private EmployeePayPeriodRepository employeePayPeriodRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private Employee testEmployee;
    private PayrollPeriod testPayPeriod;
    private List<Dependent> testDependents;

    @BeforeEach
    void setUp() {
        // Set configuration values
        ReflectionTestUtils.setField(payrollService, "employeeBenefitCostMonthly", new BigDecimal("1000.00"));
        ReflectionTestUtils.setField(payrollService, "dependentBenefitCostUnder50", new BigDecimal("600.00"));
        ReflectionTestUtils.setField(payrollService, "dependentBenefitCostOver50", new BigDecimal("800.00"));
        ReflectionTestUtils.setField(payrollService, "highSalaryThreshold", new BigDecimal("80000.00"));
        ReflectionTestUtils.setField(payrollService, "highSalaryAdditionalPercentage", new BigDecimal("0.02"));
        ReflectionTestUtils.setField(payrollService, "paychecksPerYear", 26);

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDateOfBirth(LocalDate.of(1985, 5, 15));
        testEmployee.setEmployeeStatus(EmployeeStatus.ACTIVE);

        // Create salary with dates that will pass isActiveOn() check
        EmployeePayrate payrate = new EmployeePayrate();
        ReflectionTestUtils.setField(payrate, "baseSalary", new BigDecimal("100000.00"));
        ReflectionTestUtils.setField(payrate, "startDate", LocalDateTime.now().minusYears(1));  // Started 1 year ago
        ReflectionTestUtils.setField(payrate, "endDate", LocalDateTime.now().plusYears(10));    // Ends 10 years from now
        ReflectionTestUtils.setField(payrate, "employee", testEmployee);

        List<EmployeePayrate> payrates = new ArrayList<>();
        payrates.add(payrate);
        testEmployee.setEmployeePayrates(payrates);

        // Set up pay period
        testPayPeriod = new PayrollPeriod();
        testPayPeriod.setStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        testPayPeriod.setEndDate(LocalDateTime.of(2026, 1, 14, 23, 59, 59));
        ReflectionTestUtils.setField(testPayPeriod, "id", 1L);

        // Set up dependents
        testDependents = new ArrayList<>();

        Dependent spouse = new Dependent();
        spouse.setId(1L);
        spouse.setFirstName("Jane");
        spouse.setDateOfBirth(LocalDate.of(1987, 8, 20));
        spouse.setDependentStatus(DependentStatus.ACTIVE);
        spouse.setEmployee(testEmployee);
        testDependents.add(spouse);

        Dependent child = new Dependent();
        child.setId(2L);
        child.setFirstName("Jack");
        child.setDateOfBirth(LocalDate.of(2010, 3, 10));
        child.setDependentStatus(DependentStatus.ACTIVE);
        child.setEmployee(testEmployee);
        testDependents.add(child);

        Dependent parent = new Dependent();
        parent.setId(3L);
        parent.setFirstName("Grandma");
        parent.setDateOfBirth(LocalDate.of(1950, 5, 15));
        parent.setDependentStatus(DependentStatus.ACTIVE);
        parent.setEmployee(testEmployee);
        testDependents.add(parent);
    }

    @Test
    @DisplayName("Should generate 26 pay periods for a year")
    void testGeneratePayPeriodsForYear() {
        when(payrollPeriodRepository.countPayPeriodsInYear(any(), any())).thenReturn(0L);
        when(payrollPeriodRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<PayrollPeriod> result = payrollService.generatePayPeriodsForYear(2026);

        assertNotNull(result);
        assertEquals(26, result.size());
        verify(payrollPeriodRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when pay periods already exist")
    void testGeneratePayPeriodsForYear_AlreadyExists() {
        when(payrollPeriodRepository.countPayPeriodsInYear(any(), any())).thenReturn(26L);

        assertThrows(BusinessValidationException.class, () -> {
            payrollService.generatePayPeriodsForYear(2026);
        });
        verify(payrollPeriodRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should calculate base pay correctly")
    void testCalculateBasePayPerPaycheck() {
        when(employeeRepository.findByIdAndEmployeeStatus(anyLong(), any(EmployeeStatus.class)))
                .thenReturn(Optional.of(testEmployee));

        BigDecimal result = payrollService.calculateBasePayPerPaycheck(1L);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("3846.15").compareTo(result));
    }

    @Test
    @DisplayName("Should calculate benefit deductions correctly")
    void testCalculateBenefitDeductions() {
        when(dependentRepository.findByEmployee_IdAndDependentStatus(anyLong(), any(DependentStatus.class)))
                .thenReturn(testDependents);

        BigDecimal result = payrollService.calculateBenefitDeductions(1L);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("1384.61").compareTo(result));
    }

    @Test
    @DisplayName("Should calculate additional deductions for high salary")
    void testCalculateAdditionalDeductions_HighSalary() {
        when(employeeRepository.findByIdAndEmployeeStatus(anyLong(), any(EmployeeStatus.class)))
                .thenReturn(Optional.of(testEmployee));

        BigDecimal result = payrollService.calculateAdditionalDeductions(1L);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("76.92").compareTo(result));
    }

    @Test
    @DisplayName("Should return zero additional deductions for regular salary")
    void testCalculateAdditionalDeductions_RegularSalary() {
        ReflectionTestUtils.setField(
                testEmployee.getEmployeePayrates().get(0),
                "baseSalary",
                new BigDecimal("60000.00")
        );
        when(employeeRepository.findByIdAndEmployeeStatus(anyLong(), any(EmployeeStatus.class)))
                .thenReturn(Optional.of(testEmployee));

        BigDecimal result = payrollService.calculateAdditionalDeductions(1L);

        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @Test
    @DisplayName("Should calculate net pay correctly")
    void testCalculateNetPay() {
        when(employeeRepository.findByIdAndEmployeeStatus(anyLong(), any(EmployeeStatus.class)))
                .thenReturn(Optional.of(testEmployee));
        when(dependentRepository.findByEmployee_IdAndDependentStatus(anyLong(), any(DependentStatus.class)))
                .thenReturn(testDependents);

        BigDecimal result = payrollService.calculateNetPay(1L);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("2384.62").compareTo(result));
    }

    @Test
    @DisplayName("Should detect high salary correctly")
    void testHasHighSalary_True() {
        when(employeeRepository.findByIdAndEmployeeStatus(anyLong(), any(EmployeeStatus.class)))
                .thenReturn(Optional.of(testEmployee));

        boolean result = payrollService.hasHighSalary(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should detect regular salary correctly")
    void testHasHighSalary_False() {
        ReflectionTestUtils.setField(
                testEmployee.getEmployeePayrates().get(0),
                "baseSalary",
                new BigDecimal("60000.00")
        );
        when(employeeRepository.findByIdAndEmployeeStatus(anyLong(), any(EmployeeStatus.class)))
                .thenReturn(Optional.of(testEmployee));

        boolean result = payrollService.hasHighSalary(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should calculate complete paycheck")
    void testCalculatePaycheck() {
        when(employeeRepository.findByIdAndEmployeeStatus(anyLong(), any(EmployeeStatus.class)))
                .thenReturn(Optional.of(testEmployee));
        when(payrollPeriodRepository.findById(anyLong()))
                .thenReturn(Optional.of(testPayPeriod));
        when(dependentRepository.findByEmployee_IdAndDependentStatus(anyLong(), any(DependentStatus.class)))
                .thenReturn(testDependents);

        EmployeePaycheckDto result = payrollService.calculatePaycheck(1L, 1L);

        assertNotNull(result);
        assertEquals("John", result.getEmployeeFirstName());
        assertEquals("Doe", result.getEmployeeLastName());
        assertEquals(0, new BigDecimal("3846.15").compareTo(result.getGrossPay()));
        assertEquals(0, new BigDecimal("1384.61").compareTo(result.getBenefitDeductions()));
        assertEquals(0, new BigDecimal("76.92").compareTo(result.getAdditionalDeductions()));
        assertEquals(0, new BigDecimal("2384.62").compareTo(result.getNetPay()));
    }

    @Test
    @DisplayName("Should throw exception when employee not found")
    void testCalculatePaycheck_EmployeeNotFound() {
        when(employeeRepository.findByIdAndEmployeeStatus(anyLong(), any(EmployeeStatus.class)))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            payrollService.calculatePaycheck(999L, 1L);
        });
    }

    @Test
    @DisplayName("Should throw exception when pay period not found")
    void testCalculatePaycheck_PayPeriodNotFound() {
        when(employeeRepository.findByIdAndEmployeeStatus(anyLong(), any(EmployeeStatus.class)))
                .thenReturn(Optional.of(testEmployee));
        when(payrollPeriodRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            payrollService.calculatePaycheck(1L, 999L);
        });
    }

    @Test
    @DisplayName("Should get pay period count")
    void testGetPayPeriodCount() {
        when(payrollPeriodRepository.countPayPeriodsInYear(any(), any())).thenReturn(26L);

        long result = payrollService.getPayPeriodCount(2026);

        assertEquals(26L, result);
    }
}