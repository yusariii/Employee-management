package com.khai.em.entity;

import java.time.LocalDate;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LeaveType getType() {
        return type;
    }

    public void setType(LeaveType type) {
        this.type = type;
    }

    public int getRequestedDay() {
        return requestedDay;
    }

    public void setRequestedDay(int requestedDay) {
        this.requestedDay = requestedDay;
    }

    public String getManagerComment() {
        return managerComment;
    }

    public void setManagerComment(String managerComment) {
        this.managerComment = managerComment;
    }

    public LocalDate getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDate decidedAt) {
        this.decidedAt = decidedAt;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    public void setDecidedBy(String decidedBy) {
        this.decidedBy = decidedBy;
    }

}
