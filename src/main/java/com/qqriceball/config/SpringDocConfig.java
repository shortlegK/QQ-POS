package com.qqriceball.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("飯糰點餐系統 API")
                        .version("1.0.4")
                        .description("""
                                提供點餐、查詢訂單等功能的後端 API 文件。
                                
                                **認證方式**：請先呼叫 `POST /login`，系統會自動設定 HttpOnly Cookie，後續請求無需手動設定 Token。
                               """
                        ))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("access_token")
                                        .description("HttpOnly Cookie，呼叫 /login 由 browser 自動帶入")
                        ))
        // 把這個 Scheme 當成全域預設的 security requirement
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }



}