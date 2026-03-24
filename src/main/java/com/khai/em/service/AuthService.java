package com.khai.em.service;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.khai.em.security.JwtUtils;

@Service
public class AuthService {

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

    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateToken(loginRequest.getUsername());
        return new JwtResponse(jwt, loginRequest.getUsername());
    }

    public AuthMeResponse getMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Long employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;
        String role = user.getRole() != null ? user.getRole().name() : null;

        return new AuthMeResponse(user.getUsername(), role, employeeId);
    }

    public MessageResponse register(SignupRequest signUpRequest) {
        if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
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
}
