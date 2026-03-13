package com.khai.em.dto.leavebalance.request;

import com.khai.em.entity.LeaveType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class LeaveBalanceUpsertRequest {
    @Min(2000)
    private int year;

    @NotNull(message = "type is mandatory")
    private LeaveType type;

    @Min(0)
    private int totalDays;

    public LeaveBalanceUpsertRequest() {
    }

    public LeaveBalanceUpsertRequest(int year, LeaveType type, int totalDays) {
        this.year = year;
        this.type = type;
        this.totalDays = totalDays;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public LeaveType getType() {
        return type;
    }

    public void setType(LeaveType type) {
        this.type = type;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }
}
