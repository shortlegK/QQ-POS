package com.qqriceball.controller;


import com.qqriceball.common.result.PageResult;
import com.qqriceball.common.result.Result;
import com.qqriceball.model.dto.option.*;
import com.qqriceball.model.vo.emp.EmpVO;
import com.qqriceball.model.vo.option.OptionTypeVO;
import com.qqriceball.model.vo.option.OptionVO;
import com.qqriceball.service.OptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/options")
@Slf4j
@Tag(name = "選項管理")
public class OptionController {

    private final OptionService optionService;

    @Autowired
    public OptionController(OptionService optionService) {
        this.optionService = optionService;
    }

    @Operation(summary = "4001 新增產品細節選項")
    @PostMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "產品細節選項新增成功"),
            @ApiResponse(responseCode = "409", description = "產品細節選項名稱重複"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<OptionVO> createOption(@AuthenticationPrincipal EmpVO currentEmp,
                                          @Valid @RequestBody OptionCreateDTO optionCreateDTO){

        log.info("4001 新增產品細節選項,操作id:{},參數:{}", currentEmp.getId(), optionCreateDTO);
        OptionVO optionVO = optionService.create(optionCreateDTO);
        return Result.success(optionVO);
    }

    @Operation(summary = "4002 產品細節選項分頁查詢")
    @GetMapping("/page")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<PageResult> pageQueryOption(@AuthenticationPrincipal EmpVO currentEmp,
                                              @Valid OptionPageQueryDTO optionPageQueryDTO){
        log.info("4002 產品細節選項分頁查詢,操作id:{},參數:{}", currentEmp.getId(), optionPageQueryDTO);

        PageResult pageResult = optionService.pageQuery(optionPageQueryDTO);
        return Result.success(pageResult);
    }

    @Operation(summary = "4003 修改產品細節選項")
    @PutMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "執行成功"),
            @ApiResponse(responseCode = "404", description = "產品細節選項不存在"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<OptionVO> updateOptionById(@AuthenticationPrincipal EmpVO currentEmp,
                                             @Valid @RequestBody OptionEditDTO optionEditDTO){
        log.info("4003 修改產品細節選項,操作id:{},參數:{}", currentEmp.getId(), optionEditDTO);
        OptionVO optionVO = optionService.updateById(optionEditDTO);
        return Result.success(optionVO);
    }

    @Operation (summary = "4004 根據 ID 查詢產品細節選項")
    @GetMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "404", description = "產品細節選項不存在"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<OptionVO> getOptionById(@AuthenticationPrincipal EmpVO currentEmp,
                                          @PathVariable Integer id){
        log.info("4004 根據 ID 查詢產品細節選項,操作id:{},參數:{}", currentEmp.getId(), id);
        OptionVO optionVO = optionService.getById(id);
        return Result.success(optionVO);
    }

    @Operation(summary = "4006 調整產品細節選項上架狀態")
    @PatchMapping("/{id}/status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "執行成功"),
            @ApiResponse(responseCode = "404", description = "產品細節選項不存在"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<Void> updateOptionStatus(@AuthenticationPrincipal EmpVO currentEmp,
                                            @PathVariable Integer id,
                                            @Valid @RequestBody OptionStatusDTO optionStatusDTO){
        log.info("3006 修改產品細節選項上架狀態,操作 id:{},id:{},active:{}",currentEmp.getId(),id,optionStatusDTO);
        optionService.updateStatus(id, optionStatusDTO);
        return Result.success();
    }

    @Operation(summary = "4007 取得所有選項類型")
    @GetMapping("/types")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<List<OptionTypeVO>> getOptionTypes(@AuthenticationPrincipal EmpVO currentEmp){
        log.info("4007 取得所有選項類型,操作id:{}", currentEmp.getId());
        return Result.success(optionService.getOptionTypes());
    }
}
