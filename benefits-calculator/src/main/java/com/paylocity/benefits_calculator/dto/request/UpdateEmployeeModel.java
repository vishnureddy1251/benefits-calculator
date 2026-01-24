package com.paylocity.benefits_calculator.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.paylocity.benefits_calculator.enums.EmployeeStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request model for updating an existing employee.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeModel {

    /**
     * Employee ID
     */
    @NotNull(message = "Employee ID is required")
    @Positive(message = "Employee ID must be positive")
    private Long id;

    /**
     * Employee's first name
     */
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    /**
     * Employee's last name
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    /**
     * Employee's date of birth
     */
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    /**
     * Employee status
     */
    @NotNull(message = "Employee status is required")
    private EmployeeStatus employeeStatus;

    /**
     * Employee's annual salary
     */
    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.01", message = "Salary must be greater than zero")
    @DecimalMax(value = "10000000.00", message = "Salary must be less than 10,000,000")
    private BigDecimal salary;
}