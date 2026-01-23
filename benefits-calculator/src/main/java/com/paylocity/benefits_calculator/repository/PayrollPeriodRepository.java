package com.paylocity.benefits_calculator.repository;

import com.paylocity.benefits_calculator.entity.PayrollPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {

    @Query("SELECT p FROM PayrollPeriod p " +
            "WHERE :currentDate >= p.startDate AND :currentDate <= p.endDate")
    Optional<PayrollPeriod> findCurrentPayPeriod(@Param("currentDate") LocalDateTime currentDate);

    Optional<PayrollPeriod> findByStartDateAndEndDate(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT p FROM PayrollPeriod p " +
            "WHERE p.endDate >= :startDate AND p.startDate <= :endDate " +
            "ORDER BY p.startDate")
    List<PayrollPeriod> findOverlappingPayPeriods(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<PayrollPeriod> findByStartDateAfterOrderByStartDate(LocalDateTime date);

    List<PayrollPeriod> findByEndDateBeforeOrderByEndDateDesc(LocalDateTime date);

    @Query("SELECT p FROM PayrollPeriod p " +
            "WHERE p.startDate >= :yearStart AND p.endDate <= :yearEnd " +
            "ORDER BY p.startDate")
    List<PayrollPeriod> findPayPeriodsInYear(
            @Param("yearStart") LocalDateTime yearStart,
            @Param("yearEnd") LocalDateTime yearEnd
    );

    @Query("SELECT COUNT(p) FROM PayrollPeriod p " +
            "WHERE p.startDate >= :yearStart AND p.endDate <= :yearEnd")
    long countPayPeriodsInYear(
            @Param("yearStart") LocalDateTime yearStart,
            @Param("yearEnd") LocalDateTime yearEnd
    );

    boolean existsByStartDateAndEndDate(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT p FROM PayrollPeriod p ORDER BY p.endDate DESC LIMIT 1")
    Optional<PayrollPeriod> findMostRecentPayPeriod();

    @Query("SELECT p FROM PayrollPeriod p " +
            "WHERE p.startDate > :date " +
            "ORDER BY p.startDate ASC LIMIT 1")
    Optional<PayrollPeriod> findNextPayPeriod(@Param("date") LocalDateTime date);

    @Query("SELECT p FROM PayrollPeriod p " +
            "WHERE YEAR(p.startDate) = :year " +
            "ORDER BY p.startDate")
    List<PayrollPeriod> findByYear(@Param("year") int year);
}