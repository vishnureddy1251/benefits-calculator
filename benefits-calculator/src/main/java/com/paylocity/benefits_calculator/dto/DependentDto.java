package com.paylocity.benefits_calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paylocity.benefits_calculator.enums.Gender;
import com.paylocity.benefits_calculator.enums.Relationship;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object for Dependent.
 *
 * Used to transfer dependent data between API and clients.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DependentDto {

    /**
     * Dependent ID
     */
    private Long id;

    /**
     * Dependent's first name
     */
    private String firstName;

    /**
     * Dependent's last name
     */
    private String lastName;

    /**
     * Employee ID this dependent belongs to
     */
    private Long employeeId;

    /**
     * Dependent's date of birth
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    /**
     * Relationship to employee
     */
    private Relationship relationship;

    /**
     * Gender
     */
    private Gender gender;

    /**
     * Get dependent's full name
     *
     * @return full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}