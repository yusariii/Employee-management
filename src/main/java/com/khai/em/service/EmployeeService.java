package com.khai.em.service;

import org.springframework.stereotype.Service;

import com.khai.em.dto.employee.response.EmployeeDTO;
import com.khai.em.entity.Employee;
import com.khai.em.entity.User;
import com.khai.em.repository.EmployeeRepository;
import com.khai.em.security.CurrentUserService;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    // public List<EmployeeDTO> getAllEmployees() {
    // List<Employee> employees = employeeRepository.findAll();
    // return employees.stream()
    // .map(emp -> new EmployeeDTO(emp.getId(), emp.getName(), emp.getDepartment()))
    // .toList();
    // }

    private final AuditLogService auditLogService;

    private final CurrentUserService currentUserService;

    public Page<EmployeeDTO> getAllEmployeesPagiAndSort(String keyword, int page, int size, String sortBy,
            String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Employee> employees;
        if (keyword != null && !keyword.trim().isEmpty()) {
            employees = employeeRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            employees = employeeRepository.findAll(pageable);
        }
        return employees.map(emp -> new EmployeeDTO(emp.getId(), emp.getName(), emp.getDepartment()));
    }

    @Cacheable(value = "employees", key = "#id")
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    }

    public Employee createEmployee(Employee employee) {
        if (employee.getSalary() == null) {
            throw new IllegalArgumentException("Salary is mandatory");
        }
        if (employee.getSalary() < 0) {
            throw new IllegalArgumentException("Salary must be non-negative");
        }
        auditLogService.log("CREATE", "EMPLOYEE", employee.getId(), "Employee created");
        return employeeRepository.save(employee);
    }

    @CachePut(value = "employees", key = "#id")
    public Employee updateEmployee(Long id, Employee employee) {
        Employee exist = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (employee.getName() != null) {
            exist.setName(employee.getName());
        }

        if (employee.getDepartment() != null) {
            exist.setDepartment(employee.getDepartment());
        }

        if (employee.getSalary() != null) {
            if (employee.getSalary() < 0) {
                throw new IllegalArgumentException("Salary must be non-negative");
            }
            exist.setSalary(employee.getSalary());
        }

        auditLogService.log("UPDATE", "EMPLOYEE", exist.getId(), "Employee updated");
        return employeeRepository.save(exist);
    }

    public void deleteEmployee(Long id) {
        auditLogService.log("DELETE", "EMPLOYEE", id, "Employee deleted");
        employeeRepository.deleteById(id);
    }

    public Employee getMyProfile(){
        User user = currentUserService.requireCurrentUser();
        if (user.getEmployee() == null || user.getEmployee().getId() == null) {
            throw new IllegalStateException("Employee not linked to current user");
        }
        Long employeeId = user.getEmployee().getId();
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    }
}
