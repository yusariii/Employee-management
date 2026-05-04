package com.khai.em.security;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.khai.em.repository.UserDeviceRepository;
import com.khai.em.service.EmailService;

@Component
public class AdapterAuthenticationProvider implements AuthenticationProvider {

    private static final int NEW_DEVICE_OTP_TTL_MINUTES = 10;

    private final DaoAuthenticationProvider daoAuthenticationProvider;
    private final UserDeviceRepository userDeviceRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    public AdapterAuthenticationProvider(
            DaoAuthenticationProvider daoAuthenticationProvider,
            UserDeviceRepository userDeviceRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            StringRedisTemplate redisTemplate
    ) {
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.userDeviceRepository = userDeviceRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication auth = daoAuthenticationProvider.authenticate(authentication);
        
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String clientIp = null;
        Object details = authentication.getDetails();
        if (details instanceof WebAuthenticationDetails webDetails) {
            clientIp = normalizeIp(webDetails.getRemoteAddress());
        }

        // Keep the previous IP blocking rule (adjust as needed).
        if (clientIp != null && clientIp.startsWith("192.168.")) {
            throw new AuthenticationServiceException("Access denied from IP");
        }

        if (clientIp == null) {
            return auth;
        }

        if (!(userDetails instanceof UserDetailsImpl principal)) {
            return auth;
        }

        boolean isKnownDevice = userDeviceRepository.existsByUserIdAndIpAddressAndTrustedTrue(
                principal.getId(), clientIp);

        if (!isKnownDevice) {
            String otp = emailService.generateOtp6Digits();
            String otpHash = passwordEncoder.encode(otp);

            String challengeId = UUID.randomUUID().toString();

            String challengeKey = "new_device:challenge:" + challengeId;
            String otpKey = "new_device:otp:" + challengeId;
            String attemptKey = "new_device:attempts:" + challengeId;

            String challengeValue = principal.getUsername() + "|" + principal.getId() + "|" + principal.getEmail() + "|" + clientIp;
            redisTemplate.opsForValue().set(challengeKey, challengeValue, NEW_DEVICE_OTP_TTL_MINUTES, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(otpKey, otpHash, NEW_DEVICE_OTP_TTL_MINUTES, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(attemptKey, "0", NEW_DEVICE_OTP_TTL_MINUTES, TimeUnit.MINUTES);

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(NEW_DEVICE_OTP_TTL_MINUTES);
            emailService.sendNewDeviceLoginOtpEmail(principal.getEmail(), otp, clientIp, expiresAt);

            throw new NewDeviceVerificationRequiredException(challengeId, expiresAt);
        }
        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }

    private String normalizeIp(String raw) {
        if (raw == null) {
            return null;
        }

        String ip = raw.trim();
        int commaIdx = ip.indexOf(',');
        if (commaIdx >= 0) {
            ip = ip.substring(0, commaIdx).trim();
        }
        return ip.isEmpty() ? null : ip;
    }
    
}
