package com.khai.em.service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.khai.em.security.ForwardedWebAuthenticationDetails;
import org.springframework.stereotype.Service;

import com.khai.em.dto.auth.request.LoginRequest;
import com.khai.em.dto.auth.request.SignupRequest;
import com.khai.em.dto.auth.request.VerifyNewDeviceRequest;
import com.khai.em.dto.auth.response.AuthMeResponse;
import com.khai.em.dto.auth.response.JwtResponse;
import com.khai.em.dto.auth.response.NewDeviceVerificationRequiredResponse;
import com.khai.em.dto.common.response.MessageResponse;
import com.khai.em.entity.Employee;
import com.khai.em.entity.Role;
import com.khai.em.entity.User;
import com.khai.em.entity.UserDevice;
import com.khai.em.exception.NewDeviceVerificationRequiredException;
import com.khai.em.repository.EmployeeRepository;
import com.khai.em.repository.UserDeviceRepository;
import com.khai.em.repository.UserRepository;
import com.khai.em.security.CurrentUserService;
import com.khai.em.security.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int NEW_DEVICE_OTP_MAX_ATTEMPTS = 5;

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final EmployeeRepository employeeRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    private final StringRedisTemplate redisTemplate;

    private final CurrentUserService currentUserService;

    private final UserDeviceRepository userDeviceRepository;

    public Object login(LoginRequest loginRequest, HttpServletRequest request) {

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword());

        String clientIp = resolveClientIp(request);
        authToken.setDetails(new ForwardedWebAuthenticationDetails(request, clientIp));

        // Authentication authentication = authenticationManager.authenticate(
        //         new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authToken);
        } catch (NewDeviceVerificationRequiredException ex) {
            return new NewDeviceVerificationRequiredResponse(
                    "IP_VERIFICATION_REQUIRED",
                    ex.getChallengeId(),
                    ex.getExpiresAt(),
                    "New device/IP detected. Please verify OTP sent to your email.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateToken(loginRequest.getUsername());

        String jti = jwtUtils.getJwtIdFromJwtToken(jwt);
        long remainingTimeMs = jwtUtils.getExpirationDateFromJwtToken(jwt).getTime() - System.currentTimeMillis();
        if (remainingTimeMs > 0) {
            redisTemplate.opsForValue()
                    .set("jwt:active:" + jti, loginRequest.getUsername(), remainingTimeMs, TimeUnit.MILLISECONDS);
        }

        return new JwtResponse(jwt, loginRequest.getUsername());
    }

    public JwtResponse verifyNewDevice(VerifyNewDeviceRequest request, HttpServletRequest servletRequest) {
        String clientIp = resolveClientIp(servletRequest);

        String challengeKey = "new_device:challenge:" + request.getChallengeId();
        String otpKey = "new_device:otp:" + request.getChallengeId();
        String attemptKey = "new_device:attempts:" + request.getChallengeId();

        String challengeValue = redisTemplate.opsForValue().get(challengeKey);
        String otpHash = redisTemplate.opsForValue().get(otpKey);

        if (challengeValue == null || otpHash == null) {
            throw new IllegalArgumentException("OTP expired or challenge not found");
        }

        String[] parts = challengeValue.split("\\|", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid challenge");
        }

        String username = parts[0];
        Long userId = Long.valueOf(parts[1]);
        // String email = parts[2];
        String expectedIp = parts[3];

        if (clientIp == null || !clientIp.equals(expectedIp)) {
            throw new IllegalArgumentException("Challenge is not valid for this IP");
        }

        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        if (attempts != null && attempts > NEW_DEVICE_OTP_MAX_ATTEMPTS) {
            redisTemplate.delete(Arrays.asList(challengeKey, otpKey, attemptKey));
            throw new IllegalArgumentException("Maximum OTP attempts exceeded");
        }

        if (!passwordEncoder.matches(request.getOtp(), otpHash)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        UserDevice device = userDeviceRepository.findByUserIdAndIpAddress(userId, clientIp)
                .orElseGet(() -> {
                    UserDevice d = new UserDevice();
                    d.setUserId(userId);
                    d.setIpAddress(clientIp);
                    return d;
                });
        device.setTrusted(Boolean.TRUE);
        userDeviceRepository.save(device);

        redisTemplate.delete(Arrays.asList(challengeKey, otpKey, attemptKey));

        String jwt = jwtUtils.generateToken(username);
        String jti = jwtUtils.getJwtIdFromJwtToken(jwt);
        long remainingTimeMs = jwtUtils.getExpirationDateFromJwtToken(jwt).getTime() - System.currentTimeMillis();
        if (remainingTimeMs > 0) {
            redisTemplate.opsForValue().set("jwt:active:" + jti, username, remainingTimeMs, TimeUnit.MILLISECONDS);
        }

        // emailService.sendSimpleEmail(email, "New device trusted", "IP " + clientIp + " has been trusted.");

        return new JwtResponse(jwt, username);
    }

    // NOTE: IP checks for new-device verification are handled via AuthenticationSuccessEvent listener.

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String raw = (forwarded != null && !forwarded.isBlank()) ? forwarded : request.getRemoteAddr();
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

    public AuthMeResponse getMe() {
        User user = currentUserService.requireCurrentUser();

        Long employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;
        String role = user.getRole() != null ? user.getRole().name() : null;

        return new AuthMeResponse(user.getUsername(), role, employeeId);
    }

    public MessageResponse register(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken");
        }
        if (userRepository.existsByEmployee_Id(signUpRequest.getEmployeeId())) {
            throw new IllegalArgumentException("Error: Employee already has an account");
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use");
        }
        Employee employee = employeeRepository.findById(signUpRequest.getEmployeeId()).orElse(null);
        if (employee == null) {
            throw new IllegalArgumentException("Error: Employee not found");
        }

        User user = new User();
        user.setEmployee(employee);
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.EMPLOYEE);
        userRepository.save(user);

        return new MessageResponse("User registered successfully");
    }

    public MessageResponse logout(HttpServletRequest request){
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);

            try {
                String jti = jwtUtils.getJwtIdFromJwtToken(token);
                redisTemplate.delete("jwt:active:" + jti);
            } catch (Exception ignored) {
            }
        }
        SecurityContextHolder.clearContext();
        return new MessageResponse("Logged out successfully");
    }
}
