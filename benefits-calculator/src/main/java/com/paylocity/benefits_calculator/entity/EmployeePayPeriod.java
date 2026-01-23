package com.paylocity.benefits_calculator.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "employee_pay_periods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePayPeriod extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayrollPeriod payrollPeriod;

    @Column(name = "base_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "benefits_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal benefitsAmount;

    @Column(name = "additional_benefit_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal additionalBenefitCost;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    /**
     * public EmployeePayPeriod(Employee employee, PayrollPeriod payrollPeriod,
                             BigDecimal baseAmount, BigDecimal benefitsAmount,
                             BigDecimal additionalBenefitCost, BigDecimal totalAmount) {
        this.employee = employee;
        this.payrollPeriod = payrollPeriod;
        this.baseAmount = baseAmount;
        this.benefitsAmount = benefitsAmount;
        this.additionalBenefitCost = additionalBenefitCost;
        this.totalAmount = totalAmount;
    }*/

    public void calculateTotalAmount() {
        this.totalAmount = baseAmount
                .subtract(benefitsAmount)
                .subtract(additionalBenefitCost);
    }

    public BigDecimal getTotalDeductions() {
        return benefitsAmount.add(additionalBenefitCost);
    }

    public BigDecimal getDeductionPercentage() {
        if (baseAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getTotalDeductions()
                .divide(baseAmount, 4, java.math.RoundingMode.HALF_UP);
    }

    public boolean isCalculated() {
        return baseAmount != null && baseAmount.compareTo(BigDecimal.ZERO) > 0 &&
                benefitsAmount != null &&
                additionalBenefitCost != null &&
                totalAmount != null;
    }
}