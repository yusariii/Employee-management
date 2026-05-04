package com.khai.em.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khai.em.dto.auth.request.ChangePasswordRequest;
import com.khai.em.dto.auth.request.ForgotPasswordRequest;
import com.khai.em.dto.auth.request.ResetPasswordRequest;
import com.khai.em.entity.User;
import com.khai.em.repository.UserRepository;
import com.khai.em.security.CurrentUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int OTP_TTL_MINUTES = 5;
    private static final int OTP_MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;

    private final StringRedisTemplate redisTemplate;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final AuditLogService auditLogService;

    private final CurrentUserService currentUserService;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }

        String otp = emailService.generateOtp6Digits();
        String otpHash = passwordEncoder.encode(otp);

        String otpKey = "otp:" + email;
        String attemptKey = "otp_attempts:" + email;

        redisTemplate.opsForValue().set(otpKey, otpHash, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(attemptKey, "0", OTP_TTL_MINUTES, TimeUnit.MINUTES);

        emailService.sendPasswordResetOtpEmail(email, otp, LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        String otpKey = "otp:" + email;
        String attemptKey = "otp_attempts:" + email;

        String otpHash = redisTemplate.opsForValue().get(otpKey);
        if (otpHash == null) {
            throw new IllegalArgumentException("OTP expired or not found");
        }

        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        if (attempts != null && attempts > OTP_MAX_ATTEMPTS) {
            redisTemplate.delete(Arrays.asList(otpKey, attemptKey));
            throw new IllegalArgumentException("Maximum OTP attempts exceeded");
        }

        if (!passwordEncoder.matches(request.getOtp(), otpHash)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(Arrays.asList(otpKey, attemptKey));

        auditLogService.logPublic("Reset", user, "Password", user.getId(), "Password reset successfully");
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUserService.requireCurrentUser();

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditLogService.log("Change", "Password", user.getId(), "Password changed successfully");
    }
}
