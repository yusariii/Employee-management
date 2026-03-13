package com.khai.em.dto.leave.request;

import java.time.LocalDate;

import com.khai.em.entity.LeaveType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LeaveRequestCreateRequest {

    @Size(max = 1000, message = "Reason must be at most 1000 characters")
    private String reason;

    @NotNull(message = "startDate is mandatory")
    private LocalDate startDate;

    @NotNull(message = "endDate is mandatory")
    private LocalDate endDate;

    @NotNull(message = "type is mandatory")
    private LeaveType type;

    public LeaveRequestCreateRequest() {
    }

    public LeaveRequestCreateRequest(String reason, LocalDate startDate, LocalDate endDate, LeaveType type) {
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
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

    public LeaveType getType() {
        return type;
    }

    public void setType(LeaveType type) {
        this.type = type;
    }

    @AssertTrue(message = "endDate must be on or after startDate")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }

    
}
