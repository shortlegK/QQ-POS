package com.qqriceball.server.filter;

import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;

    @Autowired
    public JwtAuthenticationTokenFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims;
        try {
            claims = JwtUtil.parseJwt(jwtProperties.getAdminSecretKey(), token);
        } catch (Exception e) {
            log.warn("JWT Token 解析失敗: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = claims.get("id", Long.class);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }
}
