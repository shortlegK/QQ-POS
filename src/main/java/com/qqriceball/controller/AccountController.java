package com.qqriceball.controller;

import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.result.Result;
import com.qqriceball.common.utils.JwtUtil;
import com.qqriceball.model.dto.emp.EmpUpdatePasswordDTO;
import com.qqriceball.model.vo.emp.EmpVO;
import com.qqriceball.model.vo.emp.TokenVO;
import com.qqriceball.service.EmpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/accounts")
@RestController
@Tag(name = "帳號管理")
public class AccountController {

    private final JwtProperties jwtProperties;
    private final EmpService empService;

    @Autowired
    public AccountController(JwtProperties jwtProperties, EmpService empService) {
        this.jwtProperties = jwtProperties;
        this.empService = empService;
    }

    @Operation(summary = "6001 更新密碼")
    @PatchMapping("/password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "密碼更新成功"),
            @ApiResponse(responseCode = "400", description = "舊密碼錯誤"),
            @ApiResponse(responseCode = "401", description = "未登入或 Token 無效"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<TokenVO> updatePassword(@AuthenticationPrincipal EmpVO currentEmp,
                                             @Valid @RequestBody EmpUpdatePasswordDTO empUpdatePasswordDTO){
        log.info("6001 員工更新密碼,操作id:{}", currentEmp.getId());
        empService.updatePassword(currentEmp.getUsername(), empUpdatePasswordDTO);

        String token = JwtUtil.generateToken(
                jwtProperties.getSecretKey(),
                currentEmp.getId(),
                currentEmp.getUsername(),
                jwtProperties.getTtlMillis());

        TokenVO tokenVO = TokenVO.builder()
                .token(token)
                .build();

        return Result.success(tokenVO);
    }
}
