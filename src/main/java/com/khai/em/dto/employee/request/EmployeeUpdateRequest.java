package com.khai.em.dto.employee.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }
}
