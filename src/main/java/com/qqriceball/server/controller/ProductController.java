package com.qqriceball.server.controller;

import com.qqriceball.common.result.Result;
import com.qqriceball.pojo.dto.ProductDTO;
import com.qqriceball.pojo.vo.EmpVO;
import com.qqriceball.server.service.ProductService;
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
@RequestMapping("/product")
@Slf4j
@Tag(name = "菜單管理")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public  ProductController(ProductService productService) {
        this.productService = productService;
    }


    @Operation(summary = "3001 新增菜單品項")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "菜單品項新增成功"),
            @ApiResponse(responseCode = "409", description = "菜單品項名稱已存在"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    @PostMapping
    public Result<String> save(@AuthenticationPrincipal EmpVO currentEmp,
            @Valid @RequestBody ProductDTO productDTO){
        log.info("3001 新增菜單品項,操作 id: {},參數:{}",currentEmp.getId(), productDTO);
        productService.saveWithOption(productDTO);
        return Result.success();
    }


}
