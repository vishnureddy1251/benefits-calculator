package com.paylocity.benefits_calculator.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.paylocity.benefits_calculator.enums.DependentStatus;
import com.paylocity.benefits_calculator.enums.Gender;
import com.paylocity.benefits_calculator.enums.Relationship;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request model for updating an existing dependent.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDependentModel {

    /**
     * Dependent ID
     */
    @NotNull(message = "Dependent ID is required")
    @Positive(message = "Dependent ID must be positive")
    private Long id;

    /**
     * Dependent's first name
     */
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    /**
     * Dependent's last name
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    /**
     * Employee ID this dependent belongs to
     */
    @NotNull(message = "Employee ID is required")
    @Positive(message = "Employee ID must be positive")
    private Long employeeId;

    /**
     * Dependent's date of birth
     */
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    /**
     * Relationship to employee
     */
    @NotNull(message = "Relationship is required")
    private Relationship relationship;

    /**
     * Dependent status
     */
    @NotNull(message = "Dependent status is required")
    private DependentStatus dependentStatus;

    /**
     * Gender
     */
    @NotNull(message = "Gender is required")
    private Gender gender;
}