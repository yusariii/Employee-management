package com.khai.em.security;

import java.time.LocalDateTime;

import org.springframework.security.core.AuthenticationException;

public class NewDeviceVerificationRequiredException extends AuthenticationException {

    private final String challengeId;
    private final LocalDateTime expiresAt;

    public NewDeviceVerificationRequiredException(String challengeId, LocalDateTime expiresAt) {
        super("IP_VERIFICATION_REQUIRED");
        this.challengeId = challengeId;
        this.expiresAt = expiresAt;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
