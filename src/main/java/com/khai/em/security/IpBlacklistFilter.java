package com.khai.em.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class IpBlacklistFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientIp = resolveClientIp(request);

        if (clientIp != null && clientIp.startsWith("192.168.")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Access denied from IP\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String raw = (forwarded != null && !forwarded.isEmpty()) ? forwarded : request.getRemoteAddr();
        if (raw == null) {
            return null;
        }

        String ip = raw.trim();
        int commaIndex = ip.indexOf(',');
        if (commaIndex >= 0) {
            ip = ip.substring(0, commaIndex).trim();
        }
        return ip.isEmpty() ? null : ip;
    }
    
}
