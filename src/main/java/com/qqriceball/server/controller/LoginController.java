package com.qqriceball.server.controller;

import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.result.Result;
import com.qqriceball.common.utils.JwtUtil;
import com.qqriceball.pojo.dto.EmpLoginDTO;
import com.qqriceball.pojo.entity.Emp;
import com.qqriceball.pojo.vo.EmpLoginVO;
import com.qqriceball.server.service.EmpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "登入登出")
public class LoginController {

    private final JwtProperties jwtProperties;
    private final EmpService empService;

    @Autowired
    public LoginController(JwtProperties jwtProperties,EmpService empService) {
        this.jwtProperties = jwtProperties;
        this.empService = empService;
    }

    @Operation(summary = "1001 登入帳號")
    @PostMapping("/login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登入成功"),
            @ApiResponse(responseCode = "401", description = "帳號或密碼錯誤"),
            @ApiResponse(responseCode = "403", description = "帳號已停用"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<EmpLoginVO> login(@RequestBody EmpLoginDTO empLoginDTO) {

        log.info("1001 登入帳號:{}", empLoginDTO);

        Emp emp = empService.login(empLoginDTO);


        String token = JwtUtil.generateToken(
                jwtProperties.getSecretKey(),
                emp.getId(),
                emp.getUsername(),
                jwtProperties.getTtlMillis());

        EmpLoginVO empLoginVO = EmpLoginVO.builder()
                .id(emp.getId())
                .username(emp.getUsername())
                .name(emp.getName())
                .token(token)
                .build();
        return Result.success(empLoginVO);
    }

    @Operation(summary = "1002 登出帳號")
    @PostMapping("/logout")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登出成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<String> logout() {
        return Result.success();
    }
}
