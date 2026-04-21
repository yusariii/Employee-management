package com.khai.em.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @Email(message = "email must be a valid email address")
    @NotBlank(message = "email is mandatory")
    private String email;
}
