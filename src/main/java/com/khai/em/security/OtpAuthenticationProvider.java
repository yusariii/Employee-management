package com.khai.em.security;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.khai.em.service.OtpLoginService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OtpAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final OtpLoginService otpLoginService;
    private final UserDetailsChecker preAuthenticationChecks = new AccountStatusUserDetailsChecker();

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String otp = (String) authentication.getCredentials();
        try {
            otpLoginService.otpLoginVerify(username, otp);
        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage(), e);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        preAuthenticationChecks.check(userDetails);
        return new OtpAuthenticationToken(userDetails, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OtpAuthenticationToken.class.isAssignableFrom(authentication);
    }
    
}
