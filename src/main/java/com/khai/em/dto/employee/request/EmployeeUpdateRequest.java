package com.khai.em.dto.employee.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

@Data
public class EmployeeUpdateRequest {

    @Pattern(regexp = ".*\\S.*", message = "name must not be blank")
    private String name;

    @Pattern(regexp = ".*\\S.*", message = "department must not be blank")
    private String department;

    @DecimalMin(value = "0", message = "Salary must be non-negative")
    private Double salary;

    public EmployeeUpdateRequest() {
    }

    public EmployeeUpdateRequest(String name, String department, Double salary) {
        this.name = name;
        this.department = department;
        this.salary = salary;
    }
}
