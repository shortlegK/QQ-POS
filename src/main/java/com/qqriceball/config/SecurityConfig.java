package com.qqriceball.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.properties.CorsProperties;
import com.qqriceball.common.result.Result;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.RoleEnum;
import com.qqriceball.filter.JwtAuthenticationTokenFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private final CorsProperties corsProperties;
    private final ObjectMapper objectMapper;

    @Autowired
    public SecurityConfig(JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter, CorsProperties corsProperties, ObjectMapper objectMapper) {
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
        this.corsProperties = corsProperties;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/login",
                                "/logout",
                                "/error",
                                // 放行 API 文件相關資源
                                "/webjars/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()
                        .requestMatchers("/emps/**").hasAuthority(RoleEnum.MANAGER.getRoleName())
                        .requestMatchers("/products/**").hasAuthority(RoleEnum.MANAGER.getRoleName())
                        .requestMatchers("/options/**").hasAuthority(RoleEnum.MANAGER.getRoleName())
                        .anyRequest().authenticated())

                .exceptionHandling(e -> e
                                .authenticationEntryPoint((req, res, ex) ->
                                        writeJsonResponse(res, HttpServletResponse.SC_UNAUTHORIZED,
                                                Result.error(MessageEnum.UNAUTHORIZED)))
                        .accessDeniedHandler((req, res, ex) ->
                                writeJsonResponse(res, HttpServletResponse.SC_FORBIDDEN,
                                        Result.error(MessageEnum.FORBIDDEN)))
                )
                .logout(logout -> logout.disable())
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void writeJsonResponse(HttpServletResponse res, int status, Result<Object> body) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
