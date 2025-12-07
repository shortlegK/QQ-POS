package com.qqriceball.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "qq-pos.jwt")
public class JwtProperties {

    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

}
