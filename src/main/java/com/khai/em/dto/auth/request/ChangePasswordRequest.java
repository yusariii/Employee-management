package com.khai.em.dto.auth.request;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {
    @NotBlank(message = "current password is mandatory")
    private String currentPassword;

    @NotBlank(message = "new password is mandatory")
    private String newPassword;

    @NotBlank(message = "confirm password is mandatory")
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
