package com.paylocity.benefits_calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for Employee.
 *
 * This DTO is used to transfer employee data between the API layer
 * and clients, separating the internal entity structure from the external API.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDto {

    /**
     * Employee ID
     */
    private Long id;

    /**
     * Employee's first name
     */
    private String firstName;

    /**
     * Employee's last name
     */
    private String lastName;

    /**
     * Employee's date of birth
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    /**
     * Current salary (from most recent payrate)
     */
    private BigDecimal salary;

    /**
     * List of dependents (null if no dependents)
     */
    private List<DependentDto> dependents;

    /**
     * Get employee's full name
     *
     * @return full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}