package com.khai.em.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


import com.khai.em.entity.Payroll;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByEmployee_IdAndMonthAndYear(Long employeeId, int month, int year);
    List<Payroll> findByMonthAndYear(int month, int year);
}
