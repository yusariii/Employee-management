package com.khai.em.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khai.em.dto.leavebalance.response.LeaveBalanceResponse;
import com.khai.em.dto.leavebalance.request.LeaveBalanceUpsertRequest;
import com.khai.em.dto.leavebalance.response.LeaveBalanceItemResponse;
import com.khai.em.repository.LeaveBalanceRepository;
import com.khai.em.repository.EmployeeRepository;
import com.khai.em.security.CurrentUserService;
import com.khai.em.entity.User;
import com.khai.em.entity.Employee;
import com.khai.em.entity.LeaveBalance;
import com.khai.em.entity.LeaveType;

import java.time.Year;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveBalanceService {
    private final LeaveBalanceRepository leaveBalanceRepository;

    private final EmployeeRepository employeeRepository;

    private final CurrentUserService currentUserService;

    private final AuditLogService auditLogService;

    public LeaveBalanceResponse getMyBalances(int year){
        User user = currentUserService.requireCurrentUser();
        Long employeeId = user.getEmployee().getId();

        var leaveBalances = leaveBalanceRepository.findByEmployee_IdAndYear(employeeId, year);

        var responseItems = leaveBalances.stream()
                .map(lb -> new LeaveBalanceItemResponse(lb.getLeaveType(), lb.getTotalDays(), lb.getUsedDays()))
                .toList();

        return new LeaveBalanceResponse(year, responseItems);
    }

    public LeaveBalanceResponse getBalancesByEmployeeId(Long employeeId, int year){
        var leaveBalances = leaveBalanceRepository.findByEmployee_IdAndYear(employeeId, year);

        var responseItems = leaveBalances.stream()
                .map(lb -> new LeaveBalanceItemResponse(lb.getLeaveType(), lb.getTotalDays(), lb.getUsedDays()))
                .toList();

        return new LeaveBalanceResponse(year, responseItems);
    }

    @Transactional
    public LeaveBalanceItemResponse upsertBalance(Long employeeId, LeaveBalanceUpsertRequest request){
        int year = request.getYear() > 0 ? request.getYear() : Year.now().getValue();
        if (request.getType() == null) {
            throw new IllegalArgumentException("type is required");
        }
        if (request.getTotalDays() < 0) {
            throw new IllegalArgumentException("totalDays must be non-negative");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        var optionalLeaveBalance = leaveBalanceRepository.findByEmployee_IdAndYearAndLeaveType(
            employeeId, year, request.getType());

        LeaveBalance leaveBalance = optionalLeaveBalance.orElseGet(() -> {
            LeaveBalance newLeaveBalance = new LeaveBalance();
            newLeaveBalance.setEmployee(employee);
            newLeaveBalance.setYear(year);
            newLeaveBalance.setLeaveType(request.getType());
            newLeaveBalance.setUsedDays(0);
            return newLeaveBalance;
        });

        leaveBalance.setTotalDays(request.getTotalDays());

        var saved = leaveBalanceRepository.save(leaveBalance);
        auditLogService.log(optionalLeaveBalance.isPresent() ? "UPDATE" : "CREATE", "LEAVE_BALANCE", saved.getId(),
                (optionalLeaveBalance.isPresent() ? "Updated" : "Created") + " leave balance for employeeId=" + employeeId + ", year=" + year + ", type=" + request.getType() + ", totalDays=" + request.getTotalDays());
        return new LeaveBalanceItemResponse(saved.getLeaveType(), saved.getTotalDays(), saved.getUsedDays());
    }

    public int getAvailableLeaveDays(Long employeeId, LeaveType type, int year) {
        if (type == null) {
            throw new IllegalArgumentException("Leave type is required");
        }
        if (type == LeaveType.UNPAID) {
            return Integer.MAX_VALUE;
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployee_IdAndYearAndLeaveType(employeeId, year, type)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Leave balance not found for employee=" + employeeId + ", year=" + year + ", type=" + type));

        return balance.getRemainingDays();
    }

    @Transactional
    public void consumeLeaveBalance(Long employeeId, LeaveType type, int year, int requestedDays) {
        if (type == null) {
            throw new IllegalArgumentException("Leave type is required");
        }
        if (type == LeaveType.UNPAID) {
            return;
        }
        if (requestedDays <= 0) {
            throw new IllegalArgumentException("requestedDays must be positive");
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployee_IdAndYearAndLeaveType(employeeId, year, type)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Leave balance not found for employee=" + employeeId + ", year=" + year + ", type=" + type));

        int remaining = balance.getRemainingDays();
        if (requestedDays > remaining) {
            throw new IllegalArgumentException(
                    "Not enough leave balance. Available: " + remaining + " days");
        }

        balance.setUsedDays(balance.getUsedDays() + requestedDays);
        leaveBalanceRepository.save(balance);
        auditLogService.log("UPDATE", "LEAVE_BALANCE", balance.getId(), "Leave balance updated for employeeId=" + employeeId + ", year=" + year + ", type=" + type + ", usedDays=" + balance.getUsedDays());
    }
}
