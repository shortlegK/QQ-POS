package com.qqriceball.controller;

import com.qqriceball.common.result.PageResult;
import com.qqriceball.common.result.Result;
import com.qqriceball.model.dto.ProductCreateDTO;
import com.qqriceball.model.dto.ProductEditDTO;
import com.qqriceball.model.dto.ProductPageQueryDTO;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.model.vo.ProductVO;
import com.qqriceball.service.ProductService;
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
@RequestMapping("/products")
@Slf4j
@Tag(name = "產品品項")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public  ProductController(ProductService productService) {
        this.productService = productService;
    }


    @Operation(summary = "3001 新增產品品項")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "產品品項新增成功"),
            @ApiResponse(responseCode = "409", description = "產品品項名稱已存在"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    @PostMapping
    public Result<ProductVO> createProduct(@AuthenticationPrincipal EmpVO currentEmp,
                                           @Valid @RequestBody ProductCreateDTO productCreateDTO){
        log.info("3001 新增產品品項,操作 id: {},參數:{}",currentEmp.getId(), productCreateDTO);
        ProductVO productVO = productService.create(productCreateDTO);
        return Result.success(productVO);
    }

    @Operation(summary = "3002 產品品項分頁查詢")
    @GetMapping("/page")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<PageResult> pageQueryProduct(@AuthenticationPrincipal EmpVO currentEmp,
                                   @Valid ProductPageQueryDTO productPageQueryDTO){
        log.info("3002 產品品項分頁查詢,操作 id: {},參數:{}",currentEmp.getId(),productPageQueryDTO);

        PageResult pageResult = productService.pageQuery(productPageQueryDTO);
        return Result.success(pageResult);
    }

    @Operation(summary = "3003 修改產品品項")
    @PutMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "執行成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<ProductVO> updateProductById(@AuthenticationPrincipal EmpVO currentEmp,
                                               @Valid @RequestBody ProductEditDTO productEditDTO){
        log.info("3003 修改產品品項,操作 id: {},參數:{}",currentEmp.getId(),productEditDTO);
        ProductVO productVO = productService.updateById(productEditDTO);
        return Result.success(productVO);
    }


    @Operation(summary = "3004 根據 ID 查詢產品品項")
    @GetMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "執行成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<ProductVO> getProductById(@AuthenticationPrincipal EmpVO currentEmp,
                                     @PathVariable Integer id){
        log.info("3004 查詢產品品項,操作 id:{},id:{}",currentEmp.getId(),id);
        ProductVO productVO = productService.getById(id);
        return Result.success(productVO);
    }

}
