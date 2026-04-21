package com.khai.em.dto.payroll.response;

import java.time.LocalDate;

import com.khai.em.entity.Employee;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PayrollResponse {
    private Long id;
    private Employee employee;
    private int month;
    private int year;
    private double baseSalary;
    private int workingDays;
    private int unpaidDays;
    private double dailyRate;
    private double unpaidDeduction;
    private double netSalary;
    private LocalDate generatedAt;
    private String generatedBy;

    public PayrollResponse() {
    }

    public PayrollResponse(Long id, Employee employee, int month, int year, double baseSalary, int workingDays,
            int unpaidDays, double dailyRate, double unpaidDeduction, double netSalary, LocalDate generatedAt,
            String generatedBy) {
        this.id = id;
        this.employee = employee;
        this.month = month;
        this.year = year;
        this.baseSalary = baseSalary;
        this.workingDays = workingDays;
        this.unpaidDays = unpaidDays;
        this.dailyRate = dailyRate;
        this.unpaidDeduction = unpaidDeduction;
        this.netSalary = netSalary;
        this.generatedAt = generatedAt;
        this.generatedBy = generatedBy;
    }
}
