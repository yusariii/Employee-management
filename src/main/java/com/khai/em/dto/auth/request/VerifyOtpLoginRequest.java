package com.khai.em.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpLoginRequest {

    @NotBlank(message = "username is mandatory")
    private String username;

    @NotBlank(message = "OTP is mandatory")
    private String otp;
}
