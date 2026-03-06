package com.qqriceball.controller;


import com.qqriceball.common.result.PageResult;
import com.qqriceball.common.result.Result;
import com.qqriceball.model.dto.emp.EmpCreateDTO;
import com.qqriceball.model.dto.emp.EmpEditDTO;
import com.qqriceball.model.dto.emp.EmpPageQueryDTO;
import com.qqriceball.model.dto.emp.EmpStatusDTO;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.service.EmpService;
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
@RequestMapping("/emps")
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
    public Result<EmpVO> createEmp(@AuthenticationPrincipal EmpVO currentEmp,
                            @Valid @RequestBody EmpCreateDTO empCreateDTO){
        log.info("2001 新增員工,操作id:{},新增帳號:{}", currentEmp.getId(), empCreateDTO.getUsername());
        EmpVO empVO = empService.create(empCreateDTO);
        return Result.success(empVO);
    }


    @Operation(summary = "2002 員工分頁查詢")
    @GetMapping("/page")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<PageResult> pageQueryEmp(@AuthenticationPrincipal EmpVO currentEmp,
                                   @Valid EmpPageQueryDTO empPageQueryDTO){
        log.info("2002 員工分頁查詢,操作id:{},參數:{}", currentEmp.getId(),empPageQueryDTO);

        PageResult pageResult = empService.pageQuery(empPageQueryDTO);

        return Result.success(pageResult);
    }


    @Operation(summary = "2003 啟用/停用員工帳號")
    @PatchMapping("/{id}/status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "執行成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<Void> updateEmpStatus(@AuthenticationPrincipal EmpVO currentEmp,
                                     @PathVariable Integer id,
                                     @Valid @RequestBody EmpStatusDTO empStatusDTO){
        log.info("2003 啟用/停用員工帳號,操作id:{},參數:{}", currentEmp.getId(), empStatusDTO);
        empService.updateStatus(empStatusDTO, id);
        return Result.success();


    }

    @Operation(summary = "2004 根據 ID 查詢員工資料")
    @GetMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "執行成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<EmpVO> getEmpById(@AuthenticationPrincipal EmpVO currentEmp,
                                      @PathVariable Integer id){
        log.info("2004 查詢員工資料,操作id:{},id:{}", currentEmp.getId(), id);
        EmpVO empVO = empService.getById(id);
        return Result.success(empVO);

    }


    @Operation(summary = "2005 修改員工資料")
    @PutMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "執行成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<EmpVO> updateEmpById(@AuthenticationPrincipal EmpVO currentEmp,
                                 @Valid @RequestBody EmpEditDTO empEditDTO){
        log.info("2005 修改員工資料,操作id:{},參數:{}", currentEmp.getId(), empEditDTO);
        EmpVO empVO = empService.updateById(empEditDTO);
        return Result.success(empVO);
    }


}
