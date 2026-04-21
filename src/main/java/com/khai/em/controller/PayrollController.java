package com.khai.em.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.khai.em.service.PayrollService;
import com.khai.em.dto.payroll.response.PayrollResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payrolls")
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
public class PayrollController {
    private final PayrollService payrollService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<List<PayrollResponse>> getEmployeePayroll(
        @RequestParam(name = "month", required = false) @Min(1) @Max(12) Integer month,
        @RequestParam(name = "year", required = false) @Min(1) Integer year
    ) {
        int m = (month != null) ? month : java.time.LocalDate.now().getMonthValue();
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        List<PayrollResponse> response = payrollService.getByMonth(m, y);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @GetMapping("/me")
    public ResponseEntity<List<PayrollResponse>> getMyPayroll(
        @RequestParam(name = "month", required = false) @Min(1) @Max(12) Integer month,
        @RequestParam(name = "year", required = false) @Min(1) Integer year
    ) {
        int m = (month != null) ? month : java.time.LocalDate.now().getMonthValue();
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        List<PayrollResponse> response = payrollService.getMyPayroll(m, y);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping("/{employeeId}/generate")
    public ResponseEntity<PayrollResponse> generatePayroll(
        @PathVariable @Positive Long employeeId,
        @RequestParam(name = "month", required = false) @Min(1) @Max(12) Integer month,
        @RequestParam(name = "year", required = false) @Min(1) Integer year
    ) {
        int m = (month != null) ? month : java.time.LocalDate.now().getMonthValue();
        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        PayrollResponse response = payrollService.generateForEmployee(employeeId, m, y);
        return ResponseEntity.ok(response);
    }
}
