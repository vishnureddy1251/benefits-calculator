package com.paylocity.benefits_calculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paylocity.benefits_calculator.dto.request.CreateEmployeeModel;
import com.paylocity.benefits_calculator.dto.request.UpdateEmployeeModel;
import com.paylocity.benefits_calculator.enums.EmployeeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.jayway.jsonpath.JsonPath;

/**
 * Integration tests for EmployeeController.
 *
 * Tests complete request/response flow including:
 * - Request mapping
 * - Validation
 * - Service integration
 * - Response formatting
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Employee Controller Integration Tests")
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create employee successfully")
    void testCreateEmployee_Success() throws Exception {
        // Arrange
        CreateEmployeeModel model = new CreateEmployeeModel();
        model.setFirstName("John");
        model.setLastName("Doe");
        model.setDateOfBirth(LocalDate.of(1985, 5, 15));
        model.setSalary(new BigDecimal("100000.00"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.salary").value(100000.00))
                .andExpect(jsonPath("$.message").value("Employee created successfully"));
    }

    @Test
    @DisplayName("Should return validation error for blank first name")
    void testCreateEmployee_BlankFirstName() throws Exception {
        // Arrange
        CreateEmployeeModel model = new CreateEmployeeModel();
        model.setFirstName("");
        model.setLastName("Doe");
        model.setDateOfBirth(LocalDate.of(1985, 5, 15));
        model.setSalary(new BigDecimal("100000.00"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Should return validation error for future date of birth")
    void testCreateEmployee_FutureDateOfBirth() throws Exception {
        // Arrange
        CreateEmployeeModel model = new CreateEmployeeModel();
        model.setFirstName("John");
        model.setLastName("Doe");
        model.setDateOfBirth(LocalDate.now().plusDays(1));
        model.setSalary(new BigDecimal("100000.00"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return validation error for negative salary")
    void testCreateEmployee_NegativeSalary() throws Exception {
        // Arrange
        CreateEmployeeModel model = new CreateEmployeeModel();
        model.setFirstName("John");
        model.setLastName("Doe");
        model.setDateOfBirth(LocalDate.of(1985, 5, 15));
        model.setSalary(new BigDecimal("-1000"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get employee by ID")
    void testGetEmployeeById() throws Exception {
        // Arrange - Create employee first
        CreateEmployeeModel model = new CreateEmployeeModel();
        model.setFirstName("Jane");
        model.setLastName("Smith");
        model.setDateOfBirth(LocalDate.of(1990, 3, 20));
        model.setSalary(new BigDecimal("80000.00"));

        String response = mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract ID from response
        Long employeeId = JsonPath.parse(response).read("$.data.id", Long.class);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Smith"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent employee")
    void testGetEmployeeById_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("Should get all employees")
    void testGetAllEmployees() throws Exception {
        // Arrange - Create multiple employees
        for (int i = 1; i <= 3; i++) {
            CreateEmployeeModel model = new CreateEmployeeModel();
            model.setFirstName("Employee" + i);
            model.setLastName("Test");
            model.setDateOfBirth(LocalDate.of(1985, 1, 1));
            model.setSalary(new BigDecimal("50000.00"));

            mockMvc.perform(post("/api/v1/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(model)));
        }

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("Should search employees by first name")
    void testSearchByFirstName() throws Exception {
        // Arrange - Create employee
        CreateEmployeeModel model = new CreateEmployeeModel();
        model.setFirstName("Alexander");
        model.setLastName("Johnson");
        model.setDateOfBirth(LocalDate.of(1985, 5, 15));
        model.setSalary(new BigDecimal("75000.00"));

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(model)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/search/firstName")
                        .param("q", "alex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].firstName").value(containsStringIgnoringCase("alex")));
    }

    @Test
    @DisplayName("Should update employee")
    void testUpdateEmployee() throws Exception {
        // Arrange - Create employee
        CreateEmployeeModel createModel = new CreateEmployeeModel();
        createModel.setFirstName("Original");
        createModel.setLastName("Name");
        createModel.setDateOfBirth(LocalDate.of(1985, 5, 15));
        createModel.setSalary(new BigDecimal("60000.00"));

        String createResponse = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createModel)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long employeeId = JsonPath.parse(createResponse).read("$.data.id", Long.class);

        // Prepare update
        UpdateEmployeeModel updateModel = new UpdateEmployeeModel();
        updateModel.setId(employeeId);
        updateModel.setFirstName("Updated");
        updateModel.setLastName("Name");
        updateModel.setDateOfBirth(LocalDate.of(1985, 5, 15));
        updateModel.setEmployeeStatus(EmployeeStatus.ACTIVE);
        updateModel.setSalary(new BigDecimal("70000.00"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateModel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.salary").value(70000.00));
    }

    @Test
    @DisplayName("Should delete employee (soft delete)")
    void testDeleteEmployee() throws Exception {
        // Arrange - Create employee
        CreateEmployeeModel model = new CreateEmployeeModel();
        model.setFirstName("ToDelete");
        model.setLastName("Employee");
        model.setDateOfBirth(LocalDate.of(1985, 5, 15));
        model.setSalary(new BigDecimal("50000.00"));

        String createResponse = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long employeeId = JsonPath.parse(createResponse).read("$.data.id", Long.class);

        // Act & Assert - Delete
        mockMvc.perform(delete("/api/v1/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Employee deleted successfully"));

        // Verify employee is inactive (should return 404)
        mockMvc.perform(get("/api/v1/employees/{id}", employeeId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get employee count")
    void testGetEmployeeCount() throws Exception {
        // Arrange - Create employees
        for (int i = 1; i <= 5; i++) {
            CreateEmployeeModel model = new CreateEmployeeModel();
            model.setFirstName("Count" + i);
            model.setLastName("Test");
            model.setDateOfBirth(LocalDate.of(1985, 1, 1));
            model.setSalary(new BigDecimal("50000.00"));

            mockMvc.perform(post("/api/v1/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(model)));
        }

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(greaterThanOrEqualTo(5)));
    }
}