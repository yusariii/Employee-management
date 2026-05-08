package com.khai.em.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpLoginRequest {
    
    @NotBlank(message = "username is mandatory")
    private String username;
}
