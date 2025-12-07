package com.qqriceball.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("飯糰點餐系統 API")
                        .version("1.0.0")
                        .description("提供點餐、查詢訂單等功能的後端 API 文件"));
    }
}