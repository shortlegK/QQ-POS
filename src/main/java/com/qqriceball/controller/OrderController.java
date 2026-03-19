package com.qqriceball.controller;


import com.qqriceball.common.result.PageResult;
import com.qqriceball.common.result.Result;
import com.qqriceball.model.dto.order.OrderCreateDTO;
import com.qqriceball.model.dto.order.OrderPageQueryDTO;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.model.vo.order.OrderSummaryVO;
import com.qqriceball.service.OrderService;
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
@RequestMapping("/orders")
@Slf4j
@Tag(name = "訂單管理")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @Operation(summary = "5001 新增訂單")
    @PostMapping("/create")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "訂單新增成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<OrderSummaryVO> createOrder(@AuthenticationPrincipal EmpVO currentEmp,
                                         @Valid @RequestBody OrderCreateDTO orderCreateDTO){
        log.info("5001 新增訂單,操作 id:{},參數:{}",currentEmp.getId(), orderCreateDTO);
        OrderSummaryVO orderSummaryVO = orderService.create(orderCreateDTO);
        return Result.success(orderSummaryVO);
    }

    @Operation(summary = "5002 訂單分頁查詢")
    @GetMapping("/page")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<PageResult> pageQueryOrder(@AuthenticationPrincipal EmpVO currentEmp,
                                             @Valid OrderPageQueryDTO orderPageQueryDTO){
        log.info("5002 訂單分頁查詢,操作 id:{},參數:{}",currentEmp.getId());
        PageResult pageResult = orderService.pageQuery(orderPageQueryDTO);
        return Result.success(pageResult);
    }

}
