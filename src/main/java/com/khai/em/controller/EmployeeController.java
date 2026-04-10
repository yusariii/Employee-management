package com.khai.em.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khai.em.dto.common.response.MessageResponse;
import com.khai.em.dto.employee.request.EmployeeCreateRequest;
import com.khai.em.dto.employee.request.EmployeeUpdateRequest;
import com.khai.em.dto.employee.response.EmployeeDTO;
import com.khai.em.entity.Employee;
import com.khai.em.service.EmployeeService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;


@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
@Validated
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> getAll(
            @RequestParam(name = "keyword", required = false) String keyword,  
            @RequestParam(name = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
            @RequestParam(name = "size", defaultValue = "5") @Min(value = 1, message = "size must be >= 1") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") @Pattern(regexp = "asc|desc", message = "sortDir must be 'asc' or 'desc'") String sortDir
    ) {
        Page<EmployeeDTO> result = employeeService.getAllEmployeesPagiAndSort(keyword, page, size, sortBy, sortDir);
        return ResponseEntity.ok(result);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable @Positive(message = "id must be positive") Long id){
        Employee emp = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(emp);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<?> getMyProfile() {
        Employee emp = employeeService.getMyProfile();
        return ResponseEntity.ok(emp);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EmployeeCreateRequest request){
        Employee employee = new Employee(request.getName(), request.getDepartment(), request.getSalary());
        employeeService.createEmployee(employee);
        return ResponseEntity.ok(employee);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable @Positive(message = "id must be positive") Long id, @Valid @RequestBody EmployeeUpdateRequest request){
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setDepartment(request.getDepartment());
        employee.setSalary(request.getSalary());
        Employee updated = employeeService.updateEmployee(id, employee);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable @Positive(message = "id must be positive") Long id){
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(new MessageResponse("Employee deleted successfully"));
    }

}
