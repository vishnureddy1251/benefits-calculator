package com.paylocity.benefits_calculator.entity;

import com.paylocity.benefits_calculator.enums.DependentStatus;
import com.paylocity.benefits_calculator.enums.Gender;
import com.paylocity.benefits_calculator.enums.Relationship;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "dependents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dependent extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false, length = 20)
    private Relationship relationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependent_status", nullable = false, length = 20)
    private DependentStatus dependentStatus = DependentStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    public Dependent(String firstName, String lastName, LocalDate dateOfBirth,
                     Employee employee, Relationship relationship, Gender gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.employee = employee;
        this.relationship = relationship;
        this.gender = gender;
        this.dependentStatus = DependentStatus.ACTIVE;
    }

    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public boolean isOver50() {
        return getAge() > 50;
    }

    public boolean isSpouseOrPartner() {
        return relationship == Relationship.SPOUSE ||
                relationship == Relationship.DOMESTIC_PARTNER;
    }

    public boolean isChild() {
        return relationship == Relationship.CHILD;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}