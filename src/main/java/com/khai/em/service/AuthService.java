package com.khai.em.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.khai.em.dto.auth.request.LoginRequest;
import com.khai.em.dto.auth.request.SignupRequest;
import com.khai.em.dto.auth.response.AuthMeResponse;
import com.khai.em.dto.auth.response.JwtResponse;
import com.khai.em.dto.common.response.MessageResponse;
import com.khai.em.entity.Employee;
import com.khai.em.entity.Role;
import com.khai.em.entity.User;
import com.khai.em.repository.EmployeeRepository;
import com.khai.em.repository.UserRepository;
import com.khai.em.security.CurrentUserService;
import com.khai.em.security.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final EmployeeRepository employeeRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    private final StringRedisTemplate redisTemplate;

    private final CurrentUserService currentUserService;

    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

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
