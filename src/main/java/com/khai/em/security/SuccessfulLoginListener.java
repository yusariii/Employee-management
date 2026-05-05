package com.khai.em.security;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.khai.em.exception.NewDeviceVerificationRequiredException;
import com.khai.em.repository.UserDeviceRepository;
import com.khai.em.service.EmailService;

@Component
public class SuccessfulLoginListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final int NEW_DEVICE_OTP_TTL_MINUTES = 10;

    private final UserDeviceRepository userDeviceRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    public SuccessfulLoginListener(
            UserDeviceRepository userDeviceRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            StringRedisTemplate redisTemplate) {
        this.userDeviceRepository = userDeviceRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();

        String clientIp = null;
        if (authentication.getDetails() instanceof WebAuthenticationDetails webDetails) {
            clientIp = normalizeIp(webDetails.getRemoteAddress());
        }

        if (clientIp == null) {
            return;
        }

        Object principal = authentication.getPrincipal();
        UserDetailsImpl userDetail = null;
        if (principal instanceof UserDetailsImpl details) {
            userDetail = details;
        }
        // else if (principal instanceof OAuth2User){

        // }
        if (userDetail == null) {
            return;
        }

        boolean isKnownDevice = userDeviceRepository.existsByUserIdAndIpAddressAndTrustedTrue(userDetail.getId(),
                clientIp);

        if (!isKnownDevice) {
            String otp = emailService.generateOtp6Digits();
            String otpHash = passwordEncoder.encode(otp);
            String challengeId = UUID.randomUUID().toString();

            String challengeKey = "new_device:challenge:" + challengeId;
            String otpKey = "new_device:otp:" + challengeId;
            String attemptKey = "new_device:attempts:" + challengeId;

            String challengeValue = userDetail.getUsername() + "|" + userDetail.getId() + "|" + userDetail.getEmail()
                    + "|" + clientIp;
            redisTemplate.opsForValue().set(challengeKey, challengeValue, NEW_DEVICE_OTP_TTL_MINUTES, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(otpKey, otpHash, NEW_DEVICE_OTP_TTL_MINUTES, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(attemptKey, "0", NEW_DEVICE_OTP_TTL_MINUTES, TimeUnit.MINUTES);

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(NEW_DEVICE_OTP_TTL_MINUTES);
            emailService.sendNewDeviceLoginOtpEmail(userDetail.getEmail(), otp, clientIp, expiresAt);

            throw new NewDeviceVerificationRequiredException(challengeId, expiresAt);
        }
    }

    private String normalizeIp(String raw) {
        if (raw == null) {
            return null;
        }
        String ip = raw.trim();
        int commaIndex = ip.indexOf(',');
        if (commaIndex >= 0) {
            ip = ip.substring(0, commaIndex).trim();
        }
        return ip.isEmpty() ? null : ip;
    }

}
