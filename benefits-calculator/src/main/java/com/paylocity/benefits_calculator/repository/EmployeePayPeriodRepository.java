package com.paylocity.benefits_calculator.repository;

import com.paylocity.benefits_calculator.entity.EmployeePayPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for EmployeePayPeriod entity.
 *
 * Provides data access methods for paycheck records including:
 * - CRUD operations
 * - Employee-specific queries
 * - Pay period queries
 * - Date range queries
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Repository
public interface EmployeePayPeriodRepository extends JpaRepository<EmployeePayPeriod, Long> {

    /**
     * Find all paychecks for a specific employee
     *
     * @param employeeId the employee ID
     * @return list of paychecks
     */
    List<EmployeePayPeriod> findByEmployee_Id(Long employeeId);

    /**
     * Find all paychecks for a specific employee with pagination
     *
     * @param employeeId the employee ID
     * @param pageable pagination parameters
     * @return page of paychecks
     */
    Page<EmployeePayPeriod> findByEmployee_Id(Long employeeId, Pageable pageable);

    /**
     * Find all paychecks for a specific pay period
     *
     * @param payrollPeriodId the pay period ID
     * @return list of paychecks
     */
    List<EmployeePayPeriod> findByPayrollPeriod_Id(Long payrollPeriodId);

    /**
     * Find paycheck for a specific employee and pay period
     *
     * @param employeeId the employee ID
     * @param payrollPeriodId the pay period ID
     * @return optional paycheck
     */
    Optional<EmployeePayPeriod> findByEmployee_IdAndPayrollPeriod_Id(
            Long employeeId,
            Long payrollPeriodId
    );

    /**
     * Check if paycheck exists for employee and pay period
     *
     * @param employeeId the employee ID
     * @param payrollPeriodId the pay period ID
     * @return true if paycheck exists
     */
    boolean existsByEmployee_IdAndPayrollPeriod_Id(
            Long employeeId,
            Long payrollPeriodId
    );

    /**
     * Find paychecks for employee within date range
     *
     * @param employeeId the employee ID
     * @param startDate start date
     * @param endDate end date
     * @return list of paychecks
     */
    @Query("SELECT epp FROM EmployeePayPeriod epp " +
            "WHERE epp.employee.id = :employeeId " +
            "AND epp.payrollPeriod.startDate >= :startDate " +
            "AND epp.payrollPeriod.endDate <= :endDate " +
            "ORDER BY epp.payrollPeriod.startDate DESC")
    List<EmployeePayPeriod> findByEmployeeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get total earnings for employee
     *
     * @param employeeId the employee ID
     * @return total net pay
     */
    @Query("SELECT SUM(epp.totalAmount) FROM EmployeePayPeriod epp " +
            "WHERE epp.employee.id = :employeeId")
    java.math.BigDecimal getTotalEarnings(@Param("employeeId") Long employeeId);

    /**
     * Get total deductions for employee
     *
     * @param employeeId the employee ID
     * @return total deductions
     */
    @Query("SELECT SUM(epp.benefitsAmount + epp.additionalBenefitCost) " +
            "FROM EmployeePayPeriod epp " +
            "WHERE epp.employee.id = :employeeId")
    java.math.BigDecimal getTotalDeductions(@Param("employeeId") Long employeeId);

    /**
     * Count paychecks for employee
     *
     * @param employeeId the employee ID
     * @return count of paychecks
     */
    long countByEmployee_Id(Long employeeId);

    /**
     * Count paychecks for pay period
     *
     * @param payrollPeriodId the pay period ID
     * @return count of paychecks
     */
    long countByPayrollPeriod_Id(Long payrollPeriodId);

    /**
     * Find most recent paycheck for employee
     *
     * @param employeeId the employee ID
     * @return optional paycheck
     */
    @Query("SELECT epp FROM EmployeePayPeriod epp " +
            "WHERE epp.employee.id = :employeeId " +
            "ORDER BY epp.payrollPeriod.startDate DESC " +
            "LIMIT 1")
    Optional<EmployeePayPeriod> findMostRecentPaycheck(@Param("employeeId") Long employeeId);

    /**
     * Delete all paychecks for a pay period (for regeneration)
     *
     * @param payrollPeriodId the pay period ID
     */
    void deleteByPayrollPeriod_Id(Long payrollPeriodId);
}