package com.qqriceball.controller;


import com.qqriceball.common.result.Result;
import com.qqriceball.model.dto.OptionCreateDTO;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.model.vo.OptionVO;
import com.qqriceball.service.OptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/options")
@Slf4j
@Tag(name = "選項管理")
public class OptionController {

    private final OptionService optionService;

    public OptionController(OptionService optionService) {
        this.optionService = optionService;
    }

    @Operation(summary = "4001 新增選項品項")
    @PostMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "選項品項新增成功"),
            @ApiResponse(responseCode = "409", description = "選項品項已存在"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    private Result<OptionVO> createOption(@AuthenticationPrincipal EmpVO currentEmp,
                                          @Valid @RequestBody OptionCreateDTO optionCreateDTO){

        log.info("4001 新增選項品項,操作id:{},參數:{}", currentEmp.getId(), optionCreateDTO);
        OptionVO optionVO = optionService.create(optionCreateDTO);
        return Result.success(optionVO);
    }




}
