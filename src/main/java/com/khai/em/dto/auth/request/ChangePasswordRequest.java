package com.khai.em.dto.auth.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotBlank(message = "current password is mandatory")
    private String currentPassword;

    @NotBlank(message = "new password is mandatory")
    private String newPassword;

    @NotBlank(message = "confirm password is mandatory")
    private String confirmPassword;
}
