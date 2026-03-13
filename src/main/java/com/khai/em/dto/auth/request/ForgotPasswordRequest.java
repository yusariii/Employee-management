package com.khai.em.dto.auth.request;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    @NotBlank(message = "email is mandatory")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
