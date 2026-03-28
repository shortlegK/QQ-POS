package com.qqriceball.controller;

import com.qqriceball.common.result.PageResult;
import com.qqriceball.common.result.Result;
import com.qqriceball.model.dto.product.*;
import com.qqriceball.model.vo.emp.EmpVO;
import com.qqriceball.model.vo.product.ProductTypeVO;
import com.qqriceball.model.vo.product.ProductVO;
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

import java.util.List;

@RestController
@RequestMapping("/products")
@Slf4j
@Tag(name = "產品管理")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public  ProductController(ProductService productService) {
        this.productService = productService;
    }


    @Operation(summary = "3001 新增產品資料")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "產品品項新增成功"),
            @ApiResponse(responseCode = "409", description = "產品品項名稱重複"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    @PostMapping
    public Result<ProductVO> createProduct(@AuthenticationPrincipal EmpVO currentEmp,
                                           @Valid @RequestBody ProductCreateDTO productCreateDTO){
        log.info("3001 新增產品品項,操作 id: {},參數:{}",currentEmp.getId(), productCreateDTO);
        ProductVO productVO = productService.create(productCreateDTO);
        return Result.success(productVO);
    }

    @Operation(summary = "3002 產品分頁查詢")
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

    @Operation(summary = "3003 修改產品資料")
    @PutMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "執行成功"),
            @ApiResponse(responseCode = "404", description = "產品品項不存在"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<ProductVO> updateProductById(@AuthenticationPrincipal EmpVO currentEmp,
                                               @Valid @RequestBody ProductEditDTO productEditDTO){
        log.info("3003 修改產品品項,操作 id: {},參數:{}",currentEmp.getId(),productEditDTO);
        ProductVO productVO = productService.updateById(productEditDTO);
        return Result.success(productVO);
    }


    @Operation(summary = "3004 根據 ID 查詢產品")
    @GetMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "404", description = "產品品項不存在"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<ProductVO> getProductById(@AuthenticationPrincipal EmpVO currentEmp,
                                     @PathVariable Integer id){
        log.info("3004 查詢產品,操作 id:{},id:{}",currentEmp.getId(),id);
        ProductVO productVO = productService.getById(id);
        return Result.success(productVO);
    }

    @Operation(summary = "3006 調整產品上架狀態")
    @PatchMapping("/{id}/status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "執行成功"),
            @ApiResponse(responseCode = "404", description = "產品品項不存在"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<Void> updateProductStatus(@AuthenticationPrincipal EmpVO currentEmp,
                                            @PathVariable Integer id,
                                            @Valid @RequestBody ProductStatusDTO productStatusDTO){
        log.info("3006 修改產品上架狀態,操作 id:{},id:{},active:{}",currentEmp.getId(),id,productStatusDTO);
        productService.updateStatus(id, productStatusDTO);
        return Result.success();
    }

    @Operation(summary = "3007 取得所有產品類型")
    @GetMapping("/types")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public Result<List<ProductTypeVO>> getAllProductTypes(@AuthenticationPrincipal EmpVO currentEmp){
        log.info("3007 取得所有產品類型,操作 id:{}",currentEmp.getId());
        return Result.success(productService.getProductTypes());
    }

}
