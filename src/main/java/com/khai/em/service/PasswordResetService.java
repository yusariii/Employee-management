package com.khai.em.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khai.em.dto.auth.request.ChangePasswordRequest;
import com.khai.em.dto.auth.request.ForgotPasswordRequest;
import com.khai.em.dto.auth.request.ResetPasswordRequest;
import com.khai.em.entity.PasswordResetOtp;
import com.khai.em.entity.User;
import com.khai.em.repository.PasswordResetOtpRepository;
import com.khai.em.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration OTP_RATE_LIMIT_WINDOW = Duration.ofMinutes(15);
    private static final int OTP_RATE_LIMIT_MAX = 3;
    private static final int OTP_MAX_ATTEMPTS = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String fromEmail;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long recentCount = passwordResetOtpRepository.countByUser_IdAndCreatedAtAfter(
                user.getId(),
                now.minus(OTP_RATE_LIMIT_WINDOW));

        if (recentCount >= OTP_RATE_LIMIT_MAX) {
            return;
        }

        // Strategy B: always remove old OTP records for this user.
        passwordResetOtpRepository.deleteByUser_Id(user.getId());

        String otp = generateOtp6Digits();
        PasswordResetOtp otpEntity = new PasswordResetOtp();
        otpEntity.setUser(user);
        otpEntity.setOtpHash(passwordEncoder.encode(otp));
        otpEntity.setAttempts(0);
        otpEntity.setCreatedAt(now);
        otpEntity.setExpiresAt(now.plus(OTP_TTL));
        otpEntity.setConsumedAt(null);
        otpEntity = passwordResetOtpRepository.save(otpEntity);

        try {
            sendOtpEmail(email, otp, otpEntity.getExpiresAt());
        } catch (MailException ex) {
            // Rollback OTP issuance if email cannot be sent.
            passwordResetOtpRepository.deleteById(otpEntity.getId());
            throw ex;
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("OTP invalid or expired"));

        LocalDateTime now = LocalDateTime.now();
        PasswordResetOtp otpEntity = passwordResetOtpRepository
                .findTopByUser_IdAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(user.getId(), now)
                .orElseThrow(() -> new IllegalArgumentException("OTP invalid or expired"));

        if (otpEntity.getAttempts() >= OTP_MAX_ATTEMPTS) {
            // Strategy B: delete all old OTP records.
            passwordResetOtpRepository.deleteByUser_Id(user.getId());
            throw new IllegalArgumentException("OTP invalid or expired");
        }

        boolean matches = passwordEncoder.matches(request.getOtp(), otpEntity.getOtpHash());
        if (!matches) {
            otpEntity.setAttempts(otpEntity.getAttempts() + 1);
            passwordResetOtpRepository.save(otpEntity);

            if (otpEntity.getAttempts() >= OTP_MAX_ATTEMPTS) {
                passwordResetOtpRepository.deleteByUser_Id(user.getId());
            }

            throw new IllegalArgumentException("OTP invalid or expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Strategy B: remove all OTP records for this user.
        passwordResetOtpRepository.deleteByUser_Id(user.getId());
        auditLogService.logPublic("Reset", user, "Password", user.getId(), "Password reset successfully");
    }

    private void sendOtpEmail(String toEmail, String otp, LocalDateTime expiresAt) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromEmail != null && !fromEmail.isBlank()) {
            message.setFrom(fromEmail);
        }
        message.setTo(toEmail);
        message.setSubject("Password reset OTP");
        message.setText("Your OTP is: " + otp + "\n\nExpires at: " + expiresAt + "\n\nIf you did not request this, please ignore this email.");
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("Unauthorized");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

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
