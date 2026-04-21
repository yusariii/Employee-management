package com.khai.em.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    private final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;

    private final StringRedisTemplate redisTemplate;

    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender mailSender;

    private final AuditLogService auditLogService;

    private final CurrentUserService currentUserService;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String fromEmail;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }

        String otp = generateOtp6Digits();
        String otpHash = passwordEncoder.encode(otp);

        String otpKey = "otp:" + email;
        String attemptKey = "otp_attempts:" + email;

        redisTemplate.opsForValue().set(otpKey, otpHash, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(attemptKey, "0", OTP_TTL_MINUTES, TimeUnit.MINUTES);

        sendOtpEmail(email, otp, LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES));
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

    private void sendOtpEmail(String toEmail, String otp, LocalDateTime expiresAt) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromEmail != null && !fromEmail.isBlank()) {
            message.setFrom(fromEmail);
        }
        message.setTo(toEmail);
        message.setSubject("Password reset OTP");
        message.setText("Your OTP is: " + otp + "\n\nExpires at: " + expiresAt
                + "\n\nIf you did not request this, please ignore this email.");
        mailSender.send(message);
    }

    private String generateOtp6Digits() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
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
