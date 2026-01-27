package com.paylocity.benefits_calculator.service;

import com.paylocity.benefits_calculator.dto.DependentDto;
import com.paylocity.benefits_calculator.dto.request.CreateDependentModel;
import com.paylocity.benefits_calculator.dto.request.UpdateDependentModel;
import com.paylocity.benefits_calculator.entity.Dependent;
import com.paylocity.benefits_calculator.entity.Employee;
import com.paylocity.benefits_calculator.enums.DependentStatus;
import com.paylocity.benefits_calculator.enums.EmployeeStatus;
import com.paylocity.benefits_calculator.enums.Gender;
import com.paylocity.benefits_calculator.enums.Relationship;
import com.paylocity.benefits_calculator.exception.ResourceNotFoundException;
import com.paylocity.benefits_calculator.repository.DependentRepository;
import com.paylocity.benefits_calculator.repository.EmployeeRepository;
import com.paylocity.benefits_calculator.service.impl.DependentServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dependent Service Tests")
class DependentServiceTest {

    @Mock
    private DependentRepository dependentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private DependentServiceImpl dependentService;

    private Employee testEmployee;
    private Dependent testDependent;
    private CreateDependentModel createModel;
    private DependentDto dependentDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dependentService, "dependentBenefitCostUnder50", new BigDecimal("600.00"));
        ReflectionTestUtils.setField(dependentService, "dependentBenefitCostOver50", new BigDecimal("800.00"));

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmployeeStatus(EmployeeStatus.ACTIVE);

        testDependent = new Dependent();
        testDependent.setId(1L);
        testDependent.setFirstName("Jane");
        testDependent.setLastName("Doe");
        testDependent.setDateOfBirth(LocalDate.of(1987, 8, 20));
        testDependent.setRelationship(Relationship.SPOUSE);
        testDependent.setGender(Gender.FEMALE);
        testDependent.setDependentStatus(DependentStatus.ACTIVE);
        testDependent.setEmployee(testEmployee);

        createModel = new CreateDependentModel();
        createModel.setFirstName("Jane");
        createModel.setLastName("Doe");
        createModel.setDateOfBirth(LocalDate.of(1987, 8, 20));
        createModel.setRelationship(Relationship.SPOUSE);
        createModel.setGender(Gender.FEMALE);
        createModel.setEmployeeId(1L);

        dependentDto = new DependentDto();
        dependentDto.setId(1L);
        dependentDto.setFirstName("Jane");
        dependentDto.setLastName("Doe");
        dependentDto.setDateOfBirth(LocalDate.of(1987, 8, 20));
        dependentDto.setRelationship(Relationship.SPOUSE);
        dependentDto.setGender(Gender.FEMALE);
    }

    @Test
    @DisplayName("Should create dependent successfully")
    void testCreateDependent_Success() {
        when(employeeRepository.findByIdAndEmployeeStatus(eq(1L), eq(EmployeeStatus.ACTIVE)))
                .thenReturn(Optional.of(testEmployee));
        when(dependentRepository.save(any(Dependent.class)))
                .thenReturn(testDependent);
        when(dependentRepository.existsSpouseOrPartnerForEmployee(anyLong(), any(DependentStatus.class)))
                .thenReturn(false);
        when(modelMapper.map(any(Dependent.class), eq(DependentDto.class)))
                .thenReturn(dependentDto);

        DependentDto result = dependentService.createDependent(createModel);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        verify(dependentRepository, times(1)).save(any(Dependent.class));
    }

    @Test
    @DisplayName("Should throw exception when employee not found")
    void testCreateDependent_EmployeeNotFound() {
        when(employeeRepository.findByIdAndEmployeeStatus(eq(1L), eq(EmployeeStatus.ACTIVE)))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            dependentService.createDependent(createModel);
        });
        verify(dependentRepository, never()).save(any(Dependent.class));
    }

    @Test
    @DisplayName("Should get dependent by ID successfully")
    void testGetDependentById_Success() {
        when(dependentRepository.findByIdAndDependentStatus(eq(1L), eq(DependentStatus.ACTIVE)))
                .thenReturn(Optional.of(testDependent));
        when(modelMapper.map(any(Dependent.class), eq(DependentDto.class)))
                .thenReturn(dependentDto);

        DependentDto result = dependentService.getDependentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should throw exception when dependent not found")
    void testGetDependentById_NotFound() {
        when(dependentRepository.findByIdAndDependentStatus(eq(999L), eq(DependentStatus.ACTIVE)))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            dependentService.getDependentById(999L);
        });
    }

    @Test
    @DisplayName("Should get all dependents")
    void testGetAllDependents() {
        List<Dependent> dependents = List.of(testDependent);
        when(dependentRepository.findByDependentStatus(eq(DependentStatus.ACTIVE)))
                .thenReturn(dependents);
        when(modelMapper.map(any(Dependent.class), eq(DependentDto.class)))
                .thenReturn(dependentDto);

        List<DependentDto> result = dependentService.getAllDependents();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get dependents by employee ID")
    void testGetDependentsByEmployeeId() {
        when(employeeRepository.existsByIdAndEmployeeStatus(eq(1L), eq(EmployeeStatus.ACTIVE)))
                .thenReturn(true);
        List<Dependent> dependents = List.of(testDependent);
        when(dependentRepository.findByEmployee_IdAndDependentStatus(eq(1L), eq(DependentStatus.ACTIVE)))
                .thenReturn(dependents);
        when(modelMapper.map(any(Dependent.class), eq(DependentDto.class)))
                .thenReturn(dependentDto);

        List<DependentDto> result = dependentService.getDependentsByEmployeeId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should update dependent successfully")
    void testUpdateDependent_Success() {
        UpdateDependentModel updateModel = new UpdateDependentModel();
        updateModel.setId(1L);
        updateModel.setFirstName("Jane");
        updateModel.setLastName("Smith");
        updateModel.setDateOfBirth(LocalDate.of(1987, 8, 20));
        updateModel.setRelationship(Relationship.SPOUSE);
        updateModel.setGender(Gender.FEMALE);
        updateModel.setEmployeeId(1L);
        updateModel.setDependentStatus(DependentStatus.ACTIVE);

        when(dependentRepository.findByIdAndDependentStatus(eq(1L), eq(DependentStatus.ACTIVE)))
                .thenReturn(Optional.of(testDependent));
        when(dependentRepository.save(any(Dependent.class)))
                .thenReturn(testDependent);
        when(modelMapper.map(any(Dependent.class), eq(DependentDto.class)))
                .thenReturn(dependentDto);

        DependentDto result = dependentService.updateDependent(updateModel);

        assertNotNull(result);
        verify(dependentRepository, times(1)).save(any(Dependent.class));
    }

    @Test
    @DisplayName("Should soft delete dependent")
    void testDeleteDependent() {
        when(dependentRepository.findByIdAndDependentStatus(eq(1L), eq(DependentStatus.ACTIVE)))
                .thenReturn(Optional.of(testDependent));

        dependentService.deleteDependent(1L);

        assertEquals(DependentStatus.INACTIVE, testDependent.getDependentStatus());
        verify(dependentRepository, times(1)).save(testDependent);
    }

    @Test
    @DisplayName("Should calculate benefit cost for dependent under 50")
    void testCalculateMonthlyBenefitCost_Under50() {
        when(dependentRepository.findByIdAndDependentStatus(eq(1L), eq(DependentStatus.ACTIVE)))
                .thenReturn(Optional.of(testDependent));

        BigDecimal result = dependentService.calculateMonthlyBenefitCost(1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("600.00"), result);
    }

    @Test
    @DisplayName("Should calculate benefit cost for dependent over 50")
    void testCalculateMonthlyBenefitCost_Over50() {
        testDependent.setDateOfBirth(LocalDate.of(1960, 1, 1));
        when(dependentRepository.findByIdAndDependentStatus(eq(1L), eq(DependentStatus.ACTIVE)))
                .thenReturn(Optional.of(testDependent));

        BigDecimal result = dependentService.calculateMonthlyBenefitCost(1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("800.00"), result);
    }

    @Test
    @DisplayName("Should get dependent count for employee")
    void testGetDependentCount() {
        when(dependentRepository.countByEmployee_IdAndDependentStatus(eq(1L), eq(DependentStatus.ACTIVE)))
                .thenReturn(3L);

        long result = dependentService.getDependentCount(1L);

        assertEquals(3L, result);
    }

    @Test
    @DisplayName("Should check if dependent exists")
    void testDependentExists() {
        when(dependentRepository.existsByIdAndDependentStatus(eq(1L), eq(DependentStatus.ACTIVE)))
                .thenReturn(true);

        boolean result = dependentService.dependentExists(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should get total dependent count")
    void testGetTotalDependentCount() {
        when(dependentRepository.countByDependentStatus(eq(DependentStatus.ACTIVE)))
                .thenReturn(10L);

        long result = dependentService.getTotalDependentCount();

        assertEquals(10L, result);
    }
}