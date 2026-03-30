package com.qqriceball.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "qq-pos.jwt")
public class JwtProperties {

    private String secretKey;
    private long ttlMillis;
    private String cookieName = "access_token";
    private boolean cookieSecure;
    private String cookieSameSite;
}


