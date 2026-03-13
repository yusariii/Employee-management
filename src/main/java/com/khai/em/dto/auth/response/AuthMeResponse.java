package com.khai.em.dto.auth.response;

public class AuthMeResponse {

    private String username;
    private String role;
    private Long employeeId;

    public AuthMeResponse() {
    }

    public AuthMeResponse(String username, String role, Long employeeId) {
        this.username = username;
        this.role = role;
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
}
