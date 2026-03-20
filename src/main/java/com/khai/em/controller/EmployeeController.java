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
import com.khai.em.dto.employee.response.EmployeeDTO;
import com.khai.em.entity.Employee;
import com.khai.em.security.UserDetailsImpl;
import com.khai.em.service.EmployeeService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        try {
            Employee emp = employeeService.getEmployeeById(id);
            return ResponseEntity.ok(emp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<?> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        try {
            Employee emp = employeeService.getEmployeeById(userId);
            return ResponseEntity.ok(emp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Employee employee){
        try {
            employeeService.createEmployee(employee);
            return ResponseEntity.ok(employee);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable @Positive(message = "id must be positive") Long id,@Valid @RequestBody Employee employee){
        try {
            Employee updated = employeeService.updateEmployee(id, employee);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable @Positive(message = "id must be positive") Long id){
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(new MessageResponse("Employee deleted successfully"));
    }

}
