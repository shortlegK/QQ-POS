package com.qqriceball.controller;


import com.qqriceball.common.result.Result;
import com.qqriceball.model.dto.revenue.RevenueDTO;
import com.qqriceball.model.vo.emp.EmpVO;
import com.qqriceball.model.vo.revenue.RevenueStatsVO;
import com.qqriceball.service.RevenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/revenue")
@Slf4j
@Tag(name = "營收統計")
public class RevenueController {

    private final RevenueService revenueService;

    @Autowired
    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @Operation(summary = "7001 查詢營收統計資料")
    @GetMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<RevenueStatsVO> getRevenueStatsByPeriodType(@AuthenticationPrincipal EmpVO currentEmp,
                                                              @Valid RevenueDTO revenueDTO) {
        log.info("7001 查詢營收統計資料,操作 id:{},參數:{}", currentEmp.getId(), revenueDTO);
        return Result.success(revenueService.getByPeriodType(revenueDTO));
    }

}
