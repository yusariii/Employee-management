package com.khai.em.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khai.em.dto.auth.request.LoginRequest;
import com.khai.em.dto.auth.request.SignupRequest;
import com.khai.em.dto.auth.request.ForgotPasswordRequest;
import com.khai.em.dto.auth.request.ResetPasswordRequest;
import com.khai.em.dto.auth.request.ChangePasswordRequest;
import com.khai.em.dto.auth.response.AuthMeResponse;
import com.khai.em.dto.auth.response.JwtResponse;
import com.khai.em.dto.common.response.MessageResponse;
import com.khai.em.entity.Employee;
import com.khai.em.entity.Role;
import com.khai.em.entity.User;
import com.khai.em.repository.UserRepository;
import com.khai.em.repository.EmployeeRepository;
import com.khai.em.security.JwtUtils;
import com.khai.em.service.PasswordResetService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateToken(loginRequest.getUsername());
        return ResponseEntity.ok(new JwtResponse(jwt, loginRequest.getUsername()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body(new MessageResponse("Unauthorized"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Long employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;
        String role = user.getRole() != null ? user.getRole().name() : null;

        return ResponseEntity.ok(new AuthMeResponse(user.getUsername(), role, employeeId));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken"));
        }
        if (userRepository.existsByEmployee_Id(signUpRequest.getEmployeeId())){
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Employee already has an account"));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use"));
        }
        Employee employee = employeeRepository.findById(signUpRequest.getEmployeeId()).orElse(null);
        if (employee == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Employee not found"));
        }

        User user = new User();
        user.setEmployee(employee);
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.EMPLOYEE);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(new MessageResponse("If the account exists, an OTP has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body(new MessageResponse("Unauthorized"));
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Password confirmation does not match"));
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Current password is incorrect"));
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: New password must be different"));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Password has been changed successfully"));
    }
}
