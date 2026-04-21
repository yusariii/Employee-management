package com.khai.em.entity;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "payrolls")
@Getter
@Setter
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
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

    public Payroll() {
    }

    public Payroll(Employee employee, int month, int year, double baseSalary, int workingDays, int unpaidDays,
            double dailyRate, double unpaidDeduction, double netSalary, LocalDate generatedAt, String generatedBy) {
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
