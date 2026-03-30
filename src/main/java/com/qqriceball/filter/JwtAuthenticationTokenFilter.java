package com.qqriceball.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.exception.AccountInactiveException;
import com.qqriceball.common.exception.ResourceNotFoundException;
import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.result.Result;
import com.qqriceball.common.utils.JwtUtil;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.RoleEnum;
import com.qqriceball.model.vo.emp.EmpVO;
import com.qqriceball.service.EmpService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final EmpService empService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationTokenFilter(JwtProperties jwtProperties, EmpService empService, ObjectMapper objectMapper) {
        this.jwtProperties = jwtProperties;
        this.empService = empService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractTokenFromCookie(request);

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = JwtUtil.parseToken(jwtProperties.getSecretKey(), token);
            String subject = claims.getSubject(); // 這裡放的是 userId
            Integer empId = Integer.valueOf(subject);

            EmpVO empVO = empService.checkActiveEmpById(empId);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (Objects.equals(empVO.getRole(), RoleEnum.MANAGER.getCode())) {
                authorities.add(new SimpleGrantedAuthority(RoleEnum.MANAGER.getRoleName()));
            } else {
                authorities.add(new SimpleGrantedAuthority(RoleEnum.STAFF.getRoleName()));
            }

            // 建立 Authentication 物件，放 userId
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(empVO, null, authorities);

            // 塞進 SecurityContext，Controller 就可以取得「目前使用者」
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        }catch (ResourceNotFoundException e) {
            log.warn("JWT 驗證失敗：帳號不存在");
            writeError(response, HttpStatus.UNAUTHORIZED, MessageEnum.ACCOUNT_NOT_EXISTS);
            return;
        }catch (AccountInactiveException e) {
            log.warn("JWT 驗證失敗：帳號已停用");
            writeError(response, HttpStatus.FORBIDDEN, MessageEnum.ACCOUNT_INACTIVE);
            return;
        }catch (ExpiredJwtException e){
            log.warn("JWT Token 已過期或無效, msg = {}", e.getMessage());
            writeError(response, HttpStatus.UNAUTHORIZED, MessageEnum.UNAUTHORIZED);
            return;
        } catch (Exception e) {
            // token 過期、簽名錯誤、格式錯誤等
            log.warn("JWT Token 解析或驗證失敗, type = {}, msg = {}",
                    e.getClass().getSimpleName(), e.getMessage());
            writeError(response, HttpStatus.UNAUTHORIZED, MessageEnum.UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // 放行登入、登出、API 文件相關資源
        return path.equals("/login") ||
                path.equals("/logout");
    }

    private void writeError(HttpServletResponse response,
                            HttpStatus status,
                            MessageEnum messageEnum) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> body = Result.error(messageEnum);
        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);
    }

    private String extractTokenFromCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for(Cookie cookie : cookies){
            if (jwtProperties.getCookieName().equals(cookie.getName())){
                return cookie.getValue();
            }
        }
        return null;
    }

}
