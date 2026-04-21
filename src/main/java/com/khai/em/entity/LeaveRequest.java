package com.khai.em.entity;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reason;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    @Enumerated(EnumType.STRING)
    private LeaveType type;

    private int requestedDay;

    @ManyToOne
    private Employee employee;

    @Nullable
    private String managerComment;

    @Nullable
    private LocalDate decidedAt;

    @Nullable
    private String decidedBy; 

    public LeaveRequest() {
    }

    public LeaveRequest(String reason, LocalDate startDate, LocalDate endDate, LeaveStatus status, LeaveType type, Employee employee) {
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.requestedDay = (int)endDate.toEpochDay() -  (int)startDate.toEpochDay() + 1;
        this.status = status;
        this.type = type;
        this.employee = employee;
    }

}
