package com.khai.em.dto.leave.request;

import com.khai.em.entity.LeaveStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LeaveRequestDecisionRequest {
    @NotNull(message = "status is mandatory")
    private LeaveStatus status;

    @Size(max = 1000, message = "managerComment must be at most 1000 characters")
    private String managerComment;

    public LeaveRequestDecisionRequest() {
    }

    public LeaveRequestDecisionRequest(LeaveStatus status, String managerComment) {
        this.status = status;
        this.managerComment = managerComment;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public String getManagerComment() {
        return managerComment;
    }

    public void setManagerComment(String managerComment) {
        this.managerComment = managerComment;
    }
}
