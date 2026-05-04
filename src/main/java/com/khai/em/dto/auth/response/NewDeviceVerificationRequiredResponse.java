package com.khai.em.dto.auth.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NewDeviceVerificationRequiredResponse {

    private String status;
    private String challengeId;
    private LocalDateTime expiresAt;
    private String message;
}
