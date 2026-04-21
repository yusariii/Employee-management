package com.khai.em.dto.employee.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeCreateRequest {

    @NotBlank(message = "name is mandatory")
    private String name;

    @NotBlank(message = "department is mandatory")
    private String department;

    @NotNull(message = "salary is mandatory")
    @DecimalMin(value = "0", message = "Salary must be non-negative")
    private Double salary;

    public EmployeeCreateRequest() {
    }

    public EmployeeCreateRequest(String name, String department, Double salary) {
        this.name = name;
        this.department = department;
        this.salary = salary;
    }
}
