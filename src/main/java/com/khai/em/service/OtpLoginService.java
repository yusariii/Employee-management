package com.khai.em.service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.khai.em.entity.User;
import com.khai.em.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpLoginService {
    private static final int OTP_TTL_MINUTES = 5;
    private static final int OTP_MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    public void otpLoginRequest(String username){

        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));

        String otp = emailService.generateOtp6Digits();
        String otpHash = passwordEncoder.encode(otp);

        String otpKey = "otp:login:" + username;
        String attemptKey = "otp:login_attempts:" + username;

        redisTemplate.opsForValue().set(otpKey, otpHash, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(attemptKey, "0", OTP_TTL_MINUTES, TimeUnit.MINUTES);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES);

        emailService.sendOtpLoginEmail(user.getEmail(), otp, expiresAt);
    }

    public boolean otpLoginVerify(String username, String otp) {
        String otpKey = "otp:login:" + username;
        String attemptKey = "otp:login_attempts:" + username;

        String otpHash = redisTemplate.opsForValue().get(otpKey);
        if (otpHash == null) {
            throw new IllegalArgumentException("OTP expired or not found");
        }

        String attemptsRaw = redisTemplate.opsForValue().get(attemptKey);
        int attempts = attemptsRaw != null ? Integer.parseInt(attemptsRaw) : 0;
        if (attempts >= OTP_MAX_ATTEMPTS) {
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptKey);
            throw new IllegalArgumentException("Maximum OTP attempts exceeded");
        }

        if (!passwordEncoder.matches(otp, otpHash)) {
            redisTemplate.opsForValue().increment(attemptKey);
            throw new IllegalArgumentException("Invalid OTP");
        }

        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptKey);

        return true;
    }
}
