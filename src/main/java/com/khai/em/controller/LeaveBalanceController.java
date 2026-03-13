package com.khai.em.controller;

import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.Year;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import com.khai.em.dto.leavebalance.request.LeaveBalanceUpsertRequest;
import com.khai.em.dto.leavebalance.response.LeaveBalanceResponse;
import com.khai.em.service.LeaveBalanceService;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/leave-balances")
@CrossOrigin(origins = "*")
@Validated
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceService leaveBalanceService;
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @GetMapping("/me")
    public ResponseEntity<LeaveBalanceResponse> getMyLeaveBalances(
        @RequestParam(name = "year", required = false) @Min(value = 2000, message = "year must be >= 2000") Integer year
    ) {
        int resolvedYear = (year != null) ? year : Year.now().getValue();
        return ResponseEntity.ok(leaveBalanceService.getMyBalances(resolvedYear));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/{employeeId}")
    public ResponseEntity<LeaveBalanceResponse> getLeaveBalancesByEmployeeId(
        @PathVariable @Positive(message = "employeeId must be positive") Long employeeId,
        @RequestParam(name = "year", required = false) @Min(value = 2000, message = "year must be >= 2000") Integer year
    ) {
        int resolvedYear = (year != null) ? year : Year.now().getValue();
        return ResponseEntity.ok(leaveBalanceService.getBalancesByEmployeeId(employeeId, resolvedYear));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{employeeId}")
    public ResponseEntity<LeaveBalanceResponse> upsertLeaveBalance(
        @PathVariable @Positive(message = "employeeId must be positive") Long employeeId,
        @Valid @RequestBody LeaveBalanceUpsertRequest request
    ) {
        int year = request.getYear() > 0 ? request.getYear() : Year.now().getValue();
        leaveBalanceService.upsertBalance(employeeId, request);
        return ResponseEntity.ok(leaveBalanceService.getBalancesByEmployeeId(employeeId, year));
    }
}
