package com.khai.em.security;

import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class OtpAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;


    public OtpAuthenticationToken(String username, String otp) {
        super((Collection<? extends GrantedAuthority>)null);
        this.principal = username;
        this.credentials = otp;
        setAuthenticated(false);
    }

    public OtpAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        setAuthenticated(true);
    }

    @Override
    public @Nullable Object getCredentials() {
        return credentials;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return principal;
    }
    
}
