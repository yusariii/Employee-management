package com.khai.em.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotNull(message = "employeeId is mandatory")
    @Positive(message = "employeeId must be positive")
    private Long employeeId;

    @Email(message = "email must be a valid email address")
    @NotBlank(message = "email is mandatory")
    private String email;

    @NotBlank(message = "username is mandatory")
    private String username;

    @NotBlank(message = "password is mandatory")
    private String password;
}
