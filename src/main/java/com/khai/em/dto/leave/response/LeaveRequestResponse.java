package com.khai.em.dto.leave.response;

import java.time.LocalDate;

import com.khai.em.entity.LeaveStatus;
import com.khai.em.entity.LeaveType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveRequestResponse {
    private Long id;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
    private LeaveType type;

    @Setter(AccessLevel.NONE)
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
}
