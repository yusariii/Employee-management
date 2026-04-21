package com.khai.em.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "leave_balances",
    uniqueConstraints = @UniqueConstraint(columnNames = { "employee_id", "year", "leave_type" })
)
@Getter
@Setter
public class LeaveBalance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private int year;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private int totalDays;
    
    @Column(nullable = false)
    private int usedDays;

    public LeaveBalance() {
    }

    public LeaveBalance(Employee employee, int year, LeaveType leaveType, int totalDays, int usedDays) {
        this.employee = employee;
        this.year = year;
        this.leaveType = leaveType;
        this.totalDays = totalDays;
        this.usedDays = usedDays;
    }

    public int getRemainingDays() {
        return totalDays - usedDays;
    }
}
