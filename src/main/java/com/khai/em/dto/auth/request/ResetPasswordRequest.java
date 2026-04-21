package com.khai.em.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @Email(message = "email must be a valid email address")
    @NotBlank(message = "email is mandatory")
    private String email;

    @NotBlank(message = "otp is mandatory")
    private String otp;

    @NotBlank(message = "new password is mandatory")
    private String newPassword;

    @NotBlank(message = "confirm password is mandatory")
    private String confirmPassword;
}
