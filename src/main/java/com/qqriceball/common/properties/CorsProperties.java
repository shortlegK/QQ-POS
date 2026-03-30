package com.qqriceball.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@ConfigurationProperties(prefix = "qq-pos.cors")
public class CorsProperties {

    private List<String> allowedOrigins;
}
