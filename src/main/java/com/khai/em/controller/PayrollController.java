package com.khai.em.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.khai.em.service.PayrollService;
import com.khai.em.dto.payroll.response.PayrollResponse;

@RestController
@RequestMapping("/api/payrolls")
@CrossOrigin(origins = "*")
@Validated
public class PayrollController {
    @Autowired
    private PayrollService payrollService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<List<PayrollResponse>> getEmployeePayroll(
        @RequestParam(name = "month", defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") @Min(1) @Max(12) int month,
        @RequestParam(name = "year", defaultValue = "#{T(java.time.LocalDate).now().getYear()}") @Min(1) int year
    ) {
        List<PayrollResponse> response = payrollService.getByMonth(month, year);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @GetMapping("/me")
    public ResponseEntity<List<PayrollResponse>> getMyPayroll(
        @RequestParam(name = "month", defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") @Min(1) @Max(12) Integer month,
        @RequestParam(name = "year", defaultValue = "#{T(java.time.LocalDate).now().getYear()}") @Min(1) Integer year
    ) {
        List<PayrollResponse> response = payrollService.getMyPayroll(month, year);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping("/{employeeId}/generate")
    public ResponseEntity<PayrollResponse> generatePayroll(
        @PathVariable @Positive Long employeeId,
        @RequestParam(name = "month", defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") @Min(1) @Max(12) int month,
        @RequestParam(name = "year", defaultValue = "#{T(java.time.LocalDate).now().getYear()}") @Min(1) int year
    ) {
        PayrollResponse response = payrollService.generateForEmployee(employeeId, month, year);
        return ResponseEntity.ok(response);
    }
}
