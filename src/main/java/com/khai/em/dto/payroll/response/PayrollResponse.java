package com.khai.em.dto.payroll.response;

import java.time.LocalDate;

import com.khai.em.entity.Employee;


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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public int getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(int workingDays) {
        this.workingDays = workingDays;
    }

    public int getUnpaidDays() {
        return unpaidDays;
    }

    public void setUnpaidDays(int unpaidDays) {
        this.unpaidDays = unpaidDays;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public double getUnpaidDeduction() {
        return unpaidDeduction;
    }

    public void setUnpaidDeduction(double unpaidDeduction) {
        this.unpaidDeduction = unpaidDeduction;
    }

    public double getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(double netSalary) {
        this.netSalary = netSalary;
    }

    public LocalDate getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDate generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }
    
}
