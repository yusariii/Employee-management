package com.khai.em.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;


import java.util.List;

import com.khai.em.dto.common.response.MessageResponse;
import com.khai.em.dto.leave.request.LeaveRequestCreateRequest;
import com.khai.em.dto.leave.response.LeaveRequestResponse;
import com.khai.em.service.LeaveRequestService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/leave-requests")
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<Page<LeaveRequestResponse>> getAllLeaveRequests() {
        Page<LeaveRequestResponse> leaveRequests = leaveRequestService.getAllLeaveRequests(Pageable.unpaged());
        return ResponseEntity.ok(leaveRequests);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @GetMapping("/me")
    public ResponseEntity<List<LeaveRequestResponse>> getMyLeaveRequests() {
        List<LeaveRequestResponse> leaveRequests = leaveRequestService.getMyLeaveRequests();
        return ResponseEntity.ok(leaveRequests);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/{employeeId}")
    public ResponseEntity<List<LeaveRequestResponse>> getLeaveRequestByEmployeeId(@PathVariable @Positive Long employeeId) {
        List<LeaveRequestResponse> leaveRequests = leaveRequestService.getLeaveRequestByEmployeeId(employeeId);
        return ResponseEntity.ok(leaveRequests);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @PostMapping
    public ResponseEntity<LeaveRequestResponse> createLeaveRequest(@Valid @RequestBody LeaveRequestCreateRequest request) {
        LeaveRequestResponse created = leaveRequestService.createLeaveRequest(request);
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping("/{leaveRequestId}/{status}")
    public ResponseEntity<LeaveRequestResponse> updateLeaveRequest(@PathVariable @Positive Long leaveRequestId,
            @PathVariable String status) {
        LeaveRequestResponse updated = leaveRequestService.updateLeaveRequestStatus(leaveRequestId, status);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/{leaveRequestId}")
    public ResponseEntity<?> deleteLeaveRequest(@PathVariable @Positive Long leaveRequestId) {
        leaveRequestService.deleteLeaveRequest(leaveRequestId);
        return ResponseEntity.ok(new MessageResponse("Leave request deleted successfully"));
    }
}
