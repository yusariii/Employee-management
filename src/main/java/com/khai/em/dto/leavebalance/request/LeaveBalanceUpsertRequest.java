package com.khai.em.dto.leavebalance.request;

import com.khai.em.entity.LeaveType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
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
}
