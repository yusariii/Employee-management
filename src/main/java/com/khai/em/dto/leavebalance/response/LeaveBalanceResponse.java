package com.khai.em.dto.leavebalance.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveBalanceResponse {
    private int year;
    private List<LeaveBalanceItemResponse> items;

    public LeaveBalanceResponse() {
    }

    public LeaveBalanceResponse(int year, List<LeaveBalanceItemResponse> items) {
        this.year = year;
        this.items = items;
    }
}
