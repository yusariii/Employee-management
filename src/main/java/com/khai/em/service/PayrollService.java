package com.khai.em.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khai.em.dto.payroll.response.PayrollResponse;
import com.khai.em.entity.Employee;
import com.khai.em.entity.LeaveRequest;
import com.khai.em.entity.LeaveStatus;
import com.khai.em.entity.LeaveType;
import com.khai.em.entity.Payroll;
import com.khai.em.entity.User;
import com.khai.em.repository.EmployeeRepository;
import com.khai.em.repository.LeaveRequestRepository;
import com.khai.em.repository.PayrollRepository;
import com.khai.em.security.CurrentUserService;

@Service
public class PayrollService {
    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private AuditLogService auditLogService;

    private PayrollResponse toResponse(Payroll payroll) {
        return new PayrollResponse(
                payroll.getId(),
                payroll.getEmployee(),
                payroll.getMonth(),
                payroll.getYear(),
                payroll.getBaseSalary(),
                payroll.getWorkingDays(),
                payroll.getUnpaidDays(),
                payroll.getDailyRate(),
                payroll.getUnpaidDeduction(),
                payroll.getNetSalary(),
                payroll.getGeneratedAt(),
                payroll.getGeneratedBy());
    }

    @Transactional
    public PayrollResponse generateForEmployee(Long employeeId, int month, int year) {

        if (employeeId == null) {
            throw new IllegalArgumentException("employeeId is required");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        if (year < 1) {
            throw new IllegalArgumentException("year must be positive");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        if (employee.getSalary() == null) {
            throw new IllegalArgumentException("Employee salary is not set for employeeId=" + employeeId);
        }

        double baseSalary = employee.getSalary();
        int workingDays = 26;
        double dailyRate = baseSalary / workingDays;

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<LeaveRequest> unpaidLeaves = leaveRequestRepository
                .findByEmployee_IdAndTypeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employeeId,
                        LeaveType.UNPAID,
                        LeaveStatus.APPROVED,
                        monthEnd,
                        monthStart);

        int unpaidDays = unpaidLeaves.stream()
                .mapToInt(lr -> calculateUnpaidDays(lr.getStartDate(), lr.getEndDate(), month, year))
                .sum();

        double unpaidDeduction = unpaidDays * dailyRate;
        double netSalary = baseSalary - unpaidDeduction;

        Payroll payroll = payrollRepository.findByEmployee_IdAndMonthAndYear(employeeId, month, year)
                .orElseGet(Payroll::new);

        payroll.setEmployee(employee);
        payroll.setMonth(month);
        payroll.setYear(year);
        payroll.setBaseSalary(baseSalary);
        payroll.setWorkingDays(workingDays);
        payroll.setUnpaidDays(unpaidDays);
        payroll.setDailyRate(dailyRate);
        payroll.setUnpaidDeduction(unpaidDeduction);
        payroll.setNetSalary(netSalary);
        payroll.setGeneratedAt(LocalDate.now());
        payroll.setGeneratedBy(currentUserService.requireCurrentUser().getUsername());

        Payroll saved = payrollRepository.save(payroll);
        auditLogService.log("GENERATE", "PAYROLL", saved.getId(), "Payroll generated for employeeId=" + employeeId + ", month=" + month + ", year=" + year);
        return toResponse(saved);
    }

    public List<PayrollResponse> getByMonth(int month, int year) {
        return payrollRepository.findByMonthAndYear(month, year).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PayrollResponse> getMyPayroll(int month, int year){
        User user = currentUserService.requireCurrentUser();
        Employee employee = user.getEmployee();

        return payrollRepository.findByEmployee_IdAndMonthAndYear(employee.getId(), month, year)
                .map(this::toResponse)
                .stream()
                .toList();
    }

    private int calculateUnpaidDays(LocalDate leaveStart, LocalDate leaveEnd, int month, int year) {
        if (leaveStart == null || leaveEnd == null) {
            return 0;
        }

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        LocalDate overlapStart = leaveStart.isAfter(monthStart) ? leaveStart : monthStart;
        LocalDate overlapEnd = leaveEnd.isBefore(monthEnd) ? leaveEnd : monthEnd;

        if (overlapEnd.isBefore(overlapStart)) {
            return 0;
        }

        long days = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
        return Math.toIntExact(days);
    }
}
