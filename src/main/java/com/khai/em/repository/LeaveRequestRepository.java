package com.khai.em.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.khai.em.entity.LeaveRequest;
import com.khai.em.entity.LeaveStatus;
import com.khai.em.entity.LeaveType;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployee_Id(Long employeeId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.startDate <= :end AND lr.endDate >= :start AND lr.status IN :statuses")
    default
    boolean existsOverlapping(Long employeeId, LocalDate start, LocalDate end, List<LeaveStatus> statuses){
        return existsByEmployee_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatusIn(employeeId, end, start, statuses);
    }

    boolean existsByEmployee_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatusIn(Long employeeId,
            LocalDate end, LocalDate start, List<LeaveStatus> statuses);

        List<LeaveRequest> findByEmployee_IdAndTypeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId,
            LeaveType type,
            LeaveStatus status,
            LocalDate end,
            LocalDate start);
} 