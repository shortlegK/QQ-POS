package com.qqriceball.controller;


import com.qqriceball.common.result.PageResult;
import com.qqriceball.common.result.Result;
import com.qqriceball.model.dto.option.OptionActiveQueryDTO;
import com.qqriceball.model.dto.option.OptionCreateDTO;
import com.qqriceball.model.dto.option.OptionEditDTO;
import com.qqriceball.model.dto.option.OptionPageQueryDTO;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.model.vo.OptionVO;
import com.qqriceball.service.OptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private Result<OptionVO> createOption(@AuthenticationPrincipal EmpVO currentEmp,
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

    @Operation(summary = "4005 根據 OptionType 查詢上架狀態的產品細節選項")
    @GetMapping("/active")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<List<OptionVO>> getActiveOptionsByType(@AuthenticationPrincipal EmpVO currentEmp,
                                                               @Valid OptionActiveQueryDTO optionActiveQueryDTO){
        log.info("4005 根據 OptionType 查詢選項,操作id:{},參數:{}", currentEmp.getId(), optionActiveQueryDTO);
        List<OptionVO> optionVOList = optionService.getActiveOptionsByType(optionActiveQueryDTO);
        return Result.success(optionVOList);
    }

}
