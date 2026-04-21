package com.khai.em.dto.leave.request;

import java.time.LocalDate;

import com.khai.em.entity.LeaveType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveRequestCreateRequest {

    @NotBlank(message = "reason is mandatory")
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

    @AssertTrue(message = "endDate must be on or after startDate")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }

    
}
