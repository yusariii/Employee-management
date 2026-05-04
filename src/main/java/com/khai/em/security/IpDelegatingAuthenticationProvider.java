package com.khai.em.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class IpDelegatingAuthenticationProvider implements AuthenticationProvider {

    private final DaoAuthenticationProvider daoAuthenticationProvider;

    public IpDelegatingAuthenticationProvider(DaoAuthenticationProvider daoAuthenticationProvider) {
        this.daoAuthenticationProvider = daoAuthenticationProvider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        Object details = authentication.getDetails();

        if (details instanceof WebAuthenticationDetails webDetails){
            String clientIp = normalizeIp(webDetails.getRemoteAddress());

            // NOTE: This only blocks the common private IPv4 range 192.168.0.0/16.
            // If you need a different rule (allowlist/CIDR), adjust here.
            if (clientIp != null && clientIp.startsWith("192.168.")){
                throw new AuthenticationServiceException("Access denied from IP");
            }
        }
        return daoAuthenticationProvider.authenticate(authentication);

    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private String normalizeIp(String raw) {
        if (raw == null) {
            return null;
        }

        // Defensive normalization (in case of proxies providing comma-separated values).
        String ip = raw.trim();
        int commaIdx = ip.indexOf(',');
        if (commaIdx >= 0) {
            ip = ip.substring(0, commaIdx).trim();
        }
        return ip.isEmpty() ? null : ip;
    }

}