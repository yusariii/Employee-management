package com.khai.em.dto.leave.response;

import java.time.LocalDate;

import com.khai.em.entity.LeaveStatus;
import com.khai.em.entity.LeaveType;
public class LeaveRequestResponse {
    private Long id;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
    private LeaveType type;
    private int requestedDay;
    private Long employeeId;
    private String managerComment;
    private LocalDate decidedAt;
    private String decidedBy;

    public LeaveRequestResponse() {
    }

    public LeaveRequestResponse(Long id, String reason, LocalDate startDate, LocalDate endDate, LeaveStatus status,
            LeaveType type, Long employeeId, String managerComment, LocalDate decidedAt, String decidedBy) {
        this.id = id;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.type = type;
        this.requestedDay =(int)endDate.toEpochDay() -  (int)startDate.toEpochDay() + 1;
        this.employeeId = employeeId;
        this.managerComment = managerComment;
        this.decidedAt = decidedAt;
        this.decidedBy = decidedBy;
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

    public LeaveType getType() {
        return type;
    }

    public void setType(LeaveType type) {
        this.type = type;
    }

    public int getRequestedDay() {
        return requestedDay;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
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
