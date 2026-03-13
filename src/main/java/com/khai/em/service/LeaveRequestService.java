package com.khai.em.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.khai.em.dto.leave.request.LeaveRequestCreateRequest;
import com.khai.em.dto.leave.response.LeaveRequestResponse;
import com.khai.em.entity.Employee;
import com.khai.em.entity.LeaveRequest;
import com.khai.em.entity.LeaveStatus;
import com.khai.em.entity.LeaveType;
import com.khai.em.entity.Role;
import com.khai.em.entity.User;
import com.khai.em.repository.LeaveRequestRepository;
import com.khai.em.security.CurrentUserService;

@Service
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private AuditLogService auditLogService;

    private LeaveRequestResponse toResponse(LeaveRequest lr) {
        Long employeeId = lr.getEmployee() != null ? lr.getEmployee().getId() : null;
        return new LeaveRequestResponse(
                lr.getId(),
                lr.getReason(),
                lr.getStartDate(),
                lr.getEndDate(),
                lr.getStatus(),
                lr.getType(),
                employeeId,
                lr.getManagerComment(),
                lr.getDecidedAt(),
                lr.getDecidedBy());
    }

    public Page<LeaveRequestResponse> getAllLeaveRequests(Pageable pageable) {
        Page<LeaveRequest> leaveRequests = leaveRequestRepository.findAll(pageable);
        return leaveRequests.map(this::toResponse);
    }

    public List<LeaveRequestResponse> getLeaveRequestByEmployeeId(Long employeeId) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByEmployee_Id(employeeId);
        if (leaveRequests.isEmpty()) {
            throw new IllegalArgumentException("Leave requests not found for employee ID: " + employeeId);
        }
        return leaveRequests.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<LeaveRequestResponse> getMyLeaveRequests() {
        User user = currentUserService.requireCurrentUser();
        Employee employee = user.getEmployee();

        if (employee == null) {
            throw new IllegalStateException("Current user is not associated with an employee");
        }

        return leaveRequestRepository.findByEmployee_Id(employee.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private int calculateRequestedDays(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return Math.toIntExact(days);
    }

    public LeaveRequestResponse createLeaveRequest(LeaveRequestCreateRequest request) {
        User user = currentUserService.requireCurrentUser();

        Employee employee = user.getEmployee();

        if (employee == null) {
            throw new IllegalStateException("Current user is not associated with an employee");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (leaveRequestRepository.existsOverlapping(employee.getId(), request.getStartDate(), request.getEndDate(), Arrays.asList(LeaveStatus.PENDING, LeaveStatus.APPROVED))) {
            throw new IllegalArgumentException("You have an overlapping leave request during this period");
        }

        int requestedDays = calculateRequestedDays(request.getStartDate(), request.getEndDate());

        if (request.getType() != LeaveType.UNPAID) {
            int availableDays = leaveBalanceService.getAvailableLeaveDays(
                    employee.getId(), request.getType(), request.getStartDate().getYear());
            if (requestedDays > availableDays) {
                throw new IllegalArgumentException("Not enough leave balance. Available: " + availableDays + " days");
            }
        }
        LeaveRequest leaveRequest = new LeaveRequest();

        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setReason(request.getReason());
        leaveRequest.setType(request.getType());
        leaveRequest.setRequestedDay(requestedDays);
        leaveRequest.setStatus(LeaveStatus.PENDING);
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        auditLogService.log("CREATE", "LEAVE_REQUEST", saved.getId(), "Leave request created");
        return toResponse(saved);
    }

    public LeaveRequestResponse updateLeaveRequestStatus(Long leaveRequestId, String status) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));

        if (!status.equalsIgnoreCase("APPROVED") && !status.equalsIgnoreCase("REJECTED")) {
            throw new IllegalArgumentException("Invalid status. Must be 'APPROVED' or 'REJECTED'");
        }

        if (status.equalsIgnoreCase("APPROVED")) {
            int requestedDays = calculateRequestedDays(leaveRequest.getStartDate(), leaveRequest.getEndDate());
            leaveBalanceService.consumeLeaveBalance(
                    leaveRequest.getEmployee().getId(),
                    leaveRequest.getType(),
                    leaveRequest.getStartDate().getYear(),
                    requestedDays);
        }

        leaveRequest.setStatus(LeaveStatus.valueOf(status.toUpperCase()));
        leaveRequest.setDecidedAt(LocalDate.now());
        leaveRequest.setDecidedBy(currentUserService.requireCurrentUser().getUsername());
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        auditLogService.log(status, "LEAVE_REQUEST", saved.getId(), "Leave request status updated to " + status.toUpperCase());
        return toResponse(saved);
    }

    public void deleteLeaveRequest(Long leaveRequestId) {
        User user = currentUserService.requireCurrentUser();

        Employee employee = user.getEmployee();

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        if (!leaveRequest.getEmployee().getId().equals(employee.getId()) && user.getRole() == Role.EMPLOYEE) {
            throw new IllegalStateException("Current user is not the owner of this leave request");
        }

        if (!leaveRequest.getStatus().equals(LeaveStatus.PENDING)) {
            throw new IllegalStateException("Only pending leave requests can be deleted");
        }

        if (!leaveRequestRepository.existsById(leaveRequestId)) {
            throw new IllegalArgumentException("Leave request not found");
        }
        auditLogService.log("DELETE", "LEAVE_REQUEST", leaveRequestId, "Leave request deleted");
        leaveRequestRepository.deleteById(leaveRequestId);
    }
}
