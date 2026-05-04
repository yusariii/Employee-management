package com.khai.em.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyNewDeviceRequest {

    @NotBlank
    private String challengeId;

    @NotBlank
    private String otp;
}
