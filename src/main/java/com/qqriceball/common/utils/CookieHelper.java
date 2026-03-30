package com.qqriceball.common.utils;

import com.qqriceball.common.properties.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieHelper {

    private final JwtProperties jwtProperties;

    public CookieHelper(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public void setTokenCookie(HttpServletResponse response, String token){
        ResponseCookie cookie = ResponseCookie
                .from(jwtProperties.getCookieName(),token)
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .path("/")
//                .maxAge(Duration.ofMillis(jwtProperties.getTtlMillis()))
                .sameSite(jwtProperties.getCookieSameSite())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());
    }

    public void clearTokenCookie(HttpServletResponse response){
        ResponseCookie cookie = ResponseCookie
                .from(jwtProperties.getCookieName(),"")
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .path("/")
                .maxAge(0)
                .sameSite(jwtProperties.getCookieSameSite())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());
    }

}
