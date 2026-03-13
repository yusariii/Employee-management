package com.khai.em.dto.leavebalance.response;

import com.khai.em.entity.LeaveType;

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

    public int getUsedDays() {
        return usedDays;
    }

    public void setUsedDays(int usedDays) {
        this.usedDays = usedDays;
    }

    public int getRemainingDays() {
        return totalDays - usedDays;
    }

}
