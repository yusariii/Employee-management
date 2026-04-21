package com.khai.em.dto.leavebalance.response;

import com.khai.em.entity.LeaveType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveBalanceItemResponse {
    private LeaveType type;
    private int totalDays;
    private int usedDays;

    public LeaveBalanceItemResponse() {
    }

    public LeaveBalanceItemResponse(LeaveType type, int totalDays, int usedDays) {
        this.type = type;
        this.totalDays = totalDays;
        this.usedDays = usedDays;
    }

    public int getRemainingDays() {
        return totalDays - usedDays;
    }

}
