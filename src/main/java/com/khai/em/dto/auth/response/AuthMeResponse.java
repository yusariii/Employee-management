package com.khai.em.dto.auth.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}
