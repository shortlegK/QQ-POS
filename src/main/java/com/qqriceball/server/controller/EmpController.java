package com.qqriceball.server.controller;


import com.qqriceball.common.result.PageResult;
import com.qqriceball.common.result.Result;
import com.qqriceball.pojo.dto.EmpCreateDTO;
import com.qqriceball.pojo.dto.EmpPageQueryDTO;
import com.qqriceball.pojo.entity.Emp;
import com.qqriceball.server.service.EmpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/emp")
@RestController
@Tag(name = "員工管理")
public class EmpController {

    private final EmpService empService;

    @Autowired
    public EmpController(EmpService empService) {
        this.empService = empService;
    }


    @Operation(summary = "2001 新增員工")
    @PostMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "員工新增成功"),
            @ApiResponse(responseCode = "409", description = "帳號已存在"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<Void> createEmp(@AuthenticationPrincipal Emp currentEmp,
                            @Valid @RequestBody EmpCreateDTO empCreateDTO){
        log.info("2001 新增員工,操作人員:{},新增帳號:{}", currentEmp.getName(), empCreateDTO.getUsername());
        empService.create(empCreateDTO, currentEmp.getName());
        return Result.success();
    }


    @Operation(summary = "2002 員工分頁查詢")
    @GetMapping("/page")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<PageResult> page(@AuthenticationPrincipal Emp currentEmp,
                                   EmpPageQueryDTO empPageQueryDTO){
        log.info("2002 員工分頁查詢,操作人員:{},參數:{}", currentEmp.getName(),empPageQueryDTO);

        PageResult pageResult = empService.pageQuery(empPageQueryDTO);

        return Result.success(pageResult);
    }


}
