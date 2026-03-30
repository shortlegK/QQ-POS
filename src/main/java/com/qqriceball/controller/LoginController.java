package com.qqriceball.controller;

import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.result.Result;
import com.qqriceball.common.utils.CookieHelper;
import com.qqriceball.common.utils.JwtUtil;
import com.qqriceball.model.dto.emp.EmpLoginDTO;
import com.qqriceball.model.entity.Emp;
import com.qqriceball.model.vo.emp.EmpLoginVO;
import com.qqriceball.model.vo.emp.EmpVO;
import com.qqriceball.service.EmpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "登入登出")
public class LoginController {

    private final JwtProperties jwtProperties;
    private final EmpService empService;
    private final CookieHelper cookieHelper;

    @Autowired
    public LoginController(JwtProperties jwtProperties,EmpService empService, CookieHelper cookieHelper) {
        this.jwtProperties = jwtProperties;
        this.empService = empService;
        this.cookieHelper = cookieHelper;
    }

    @Operation(summary = "1001 登入帳號")
    @PostMapping("/login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登入成功"),
            @ApiResponse(responseCode = "401", description = "帳號或密碼錯誤"),
            @ApiResponse(responseCode = "403", description = "帳號已停用"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<EmpLoginVO> login(@Valid @RequestBody EmpLoginDTO empLoginDTO,
                                    HttpServletResponse response) {

        log.info("1001 登入帳號:{}", empLoginDTO);

        Emp emp = empService.login(empLoginDTO);


        String token = JwtUtil.generateToken(
                jwtProperties.getSecretKey(),
                emp.getId(),
                emp.getUsername(),
                jwtProperties.getTtlMillis());

        cookieHelper.setTokenCookie(response,token);

        EmpLoginVO empLoginVO = EmpLoginVO.builder()
                .id(emp.getId())
                .username(emp.getUsername())
                .name(emp.getName())
                .role(emp.getRole())
                .build();

        return Result.success(empLoginVO);
    }

    @Operation(summary = "1002 登出帳號")
    @PostMapping("/logout")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登出成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<String> logout(HttpServletResponse response) {
        cookieHelper.clearTokenCookie(response);
        return Result.success();
    }

    @Operation(summary = "1003 刷新 Token")
    @PostMapping("/token/refresh")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "執行成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<Void> refreshToken(@AuthenticationPrincipal EmpVO currentEmp,
                                     HttpServletResponse response) {
        log.info("1003 刷新 Token,操作id:{}", currentEmp.getId());
            String token = JwtUtil.generateToken(
                    jwtProperties.getSecretKey(),
                    currentEmp.getId(),
                    currentEmp.getUsername(),
                    jwtProperties.getTtlMillis());

            cookieHelper.setTokenCookie(response,token);

        return Result.success();
    }

}
