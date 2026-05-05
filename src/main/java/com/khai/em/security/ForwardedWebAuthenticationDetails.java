package com.khai.em.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class ForwardedWebAuthenticationDetails extends WebAuthenticationDetails {

    private final String forwardedRemoteAddress;

    public ForwardedWebAuthenticationDetails(HttpServletRequest request, String forwardedRemoteAddress) {
        super(request);
        this.forwardedRemoteAddress = forwardedRemoteAddress;
    }

    @Override
    public String getRemoteAddress() {
        return forwardedRemoteAddress != null ? forwardedRemoteAddress : super.getRemoteAddress();
    }
}
