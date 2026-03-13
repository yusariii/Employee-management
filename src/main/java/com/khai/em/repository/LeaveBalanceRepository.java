package com.khai.em.repository;


import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.khai.em.entity.LeaveType;
import com.khai.em.entity.LeaveBalance;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    
    Optional<LeaveBalance> findByEmployee_IdAndYearAndLeaveType(Long employeeId, int year, LeaveType leaveType);
    List<LeaveBalance> findByEmployee_IdAndYear(Long employeeId, int year);
}
