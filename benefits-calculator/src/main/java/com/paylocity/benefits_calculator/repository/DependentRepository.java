package com.paylocity.benefits_calculator.repository;

import com.paylocity.benefits_calculator.entity.Dependent;
import com.paylocity.benefits_calculator.enums.DependentStatus;
import com.paylocity.benefits_calculator.enums.Relationship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DependentRepository extends JpaRepository<Dependent, Long> {

    Optional<Dependent> findByIdAndDependentStatus(Long id, DependentStatus status);

    Page<Dependent> findAllByDependentStatus(DependentStatus status, Pageable pageable);

    Page<Dependent> findByEmployee_IdAndDependentStatus(
            Long employeeId,
            DependentStatus status,
            Pageable pageable
    );

    List<Dependent> findByEmployee_IdAndDependentStatus(
            Long employeeId,
            DependentStatus status
    );

    List<Dependent> findByEmployee_Id(Long employeeId);

    long countByEmployee_IdAndDependentStatus(Long employeeId, DependentStatus status);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
            "FROM Dependent d " +
            "WHERE d.employee.id = :employeeId " +
            "AND d.dependentStatus = :status " +
            "AND (d.relationship = 'SPOUSE' OR d.relationship = 'DOMESTIC_PARTNER')")
    boolean existsSpouseOrPartnerForEmployee(
            @Param("employeeId") Long employeeId,
            @Param("status") DependentStatus status
    );

    @Query("SELECT d FROM Dependent d " +
            "WHERE d.employee.id = :employeeId " +
            "AND d.dependentStatus = :status " +
            "AND (d.relationship = 'SPOUSE' OR d.relationship = 'DOMESTIC_PARTNER')")
    Optional<Dependent> findSpouseOrPartnerForEmployee(
            @Param("employeeId") Long employeeId,
            @Param("status") DependentStatus status
    );

    List<Dependent> findByEmployee_IdAndRelationshipAndDependentStatus(
            Long employeeId,
            Relationship relationship,
            DependentStatus status
    );

    @Query("SELECT COUNT(d) FROM Dependent d " +
            "WHERE d.employee.id = :employeeId " +
            "AND d.dependentStatus = :status " +
            "AND d.dateOfBirth < :cutoffDate")
    long countDependentsOver50(
            @Param("employeeId") Long employeeId,
            @Param("cutoffDate") java.time.LocalDate cutoffDate,
            @Param("status") DependentStatus status
    );

    boolean existsByIdAndDependentStatus(Long id, DependentStatus status);

    List<Dependent> findByFirstNameContainingIgnoreCaseAndDependentStatus(
            String firstName,
            DependentStatus status
    );

    List<Dependent> findByLastNameContainingIgnoreCaseAndDependentStatus(
            String lastName,
            DependentStatus status
    );
}
