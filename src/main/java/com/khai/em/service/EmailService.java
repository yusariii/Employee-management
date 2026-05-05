package com.khai.em.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String fromEmail;

    public String generateOtp6Digits() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    @Async
    public void sendPasswordResetOtpEmail(String toEmail, String otp, LocalDateTime expiresAt) {
        String subject = "Password reset OTP";
        String text = "Your OTP is: " + otp + "\n\nExpires at: " + expiresAt
                + "\n\nIf you did not request this, please ignore this email.";

        sendSimpleEmail(toEmail, subject, text);
    }

    public void sendSimpleEmail(String toEmail, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromEmail != null && !fromEmail.isBlank()) {
            message.setFrom(fromEmail);
        }
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    @Async
    public void sendNewDeviceLoginOtpEmail(String email, String otp, String clientIp, LocalDateTime expiresAt) {
        String subject = "Verify new device login";
        String text = "We detected a login to your account from a new device or location (IP: " + clientIp + ")."
                + "\n\nYour OTP is: " + otp
                + "\nExpires at: " + expiresAt
                + "\n\nIf you did not attempt to log in, please change your password immediately.";

        sendSimpleEmail(email, subject, text);
    }
}
