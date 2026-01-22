package com.paylocity.benefits_calculator.entity;

import com.paylocity.benefits_calculator.enums.EmployeeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an Employee in the benefits calculator system.
 *
 * An employee can have multiple dependents and a salary history (payrates).
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends BaseEntity {

    /**
     * Employee's first name
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /**
     * Employee's last name
     */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * Employee's date of birth
     */
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * Current status of the employee (PENDING, ACTIVE, INACTIVE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_status", nullable = false, length = 20)
    private EmployeeStatus employeeStatus = EmployeeStatus.ACTIVE;

    /**
     * List of dependents associated with this employee.
     * Cascade operations ensure dependents are managed with the employee.
     * Orphan removal ensures dependents are deleted when removed from the list.
     */
    @OneToMany(
            mappedBy = "employee",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Dependent> dependents = new ArrayList<>();

    /**
     * List of salary/payrate history for this employee.
     * Tracks all salary changes over time.
     */
    @OneToMany(
            mappedBy = "employee",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<EmployeePayrate> employeePayrates = new ArrayList<>();

    /**
     * Helper method to add a dependent to this employee.
     * Maintains bidirectional relationship.
     *
     * @param dependent the dependent to add
     */
    public void addDependent(Dependent dependent) {
        dependents.add(dependent);
        dependent.setEmployee(this);
    }

    /**
     * Helper method to remove a dependent from this employee.
     * Maintains bidirectional relationship.
     *
     * @param dependent the dependent to remove
     */
    public void removeDependent(Dependent dependent) {
        dependents.remove(dependent);
        dependent.setEmployee(null);
    }

    /**
     * Helper method to add a payrate to this employee.
     * Maintains bidirectional relationship.
     *
     * @param payrate the payrate to add
     */
    public void addPayrate(EmployeePayrate payrate) {
        employeePayrates.add(payrate);
        payrate.setEmployee(this);
    }

    /**
     * Helper method to remove a payrate from this employee.
     * Maintains bidirectional relationship.
     *
     * @param payrate the payrate to remove
     */
    public void removePayrate(EmployeePayrate payrate) {
        employeePayrates.remove(payrate);
        payrate.setEmployee(null);
    }
}