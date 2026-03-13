package com.khai.em.dto.leavebalance.response;

import java.util.List;

public class LeaveBalanceResponse {
    private int year;
    private List<LeaveBalanceItemResponse> items;

    public LeaveBalanceResponse() {
    }

    public LeaveBalanceResponse(int year, List<LeaveBalanceItemResponse> items) {
        this.year = year;
        this.items = items;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<LeaveBalanceItemResponse> getItems() {
        return items;
    }

    public void setItems(List<LeaveBalanceItemResponse> items) {
        this.items = items;
    }
}
