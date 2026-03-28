package com.qqriceball.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.exception.ResourceNotFoundException;
import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.controller.ProductController;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.handler.GlobalExceptionHandler;
import com.qqriceball.model.dto.product.*;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.model.vo.ProductVO;
import com.qqriceball.service.EmpService;
import com.qqriceball.service.ProductService;
import com.qqriceball.utils.product.ProductTestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtProperties jwtProperties;

    @MockBean
    EmpService empService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUpAuth() {
        EmpVO emp = new EmpVO();
        emp.setId(99);
        Authentication auth = new UsernamePasswordAuthenticationToken(emp, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void cleanAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("[Unit] ProductController.createProduct() - 建立重複品項，應回傳 409 及指定訊息")
    void testCreateProductTitleDuplicate() throws Exception {

        ProductCreateDTO productCreateDTO = ProductTestDataFactory.getProductCreateDTO(SeedProductData.MEAT_PRODUCT);

        doThrow(new AlreadyExistsException(MessageEnum.PRODUCT_ALREADY_EXISTS))
                .when(productService)
                .create(any(ProductCreateDTO.class));

        String jsonBody = objectMapper.writeValueAsString(productCreateDTO);
        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_ALREADY_EXISTS.getCode()));
    }

    @Test
    @DisplayName("[Unit] ProductController.createProduct() - 建立品項成功，應回傳 200")
    void testCreateProductSuccess() throws Exception {

        ProductCreateDTO productCreateDTO = ProductTestDataFactory.getProductCreateDTO(SeedProductData.MEAT_PRODUCT);

        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(productCreateDTO, productVO);
        when(productService.create(any(ProductCreateDTO.class))).thenReturn(productVO);

        String jsonBody = objectMapper.writeValueAsString(productCreateDTO);
        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("[Unit] ProductController.createProduct() - 建立品項參數錯誤，應回傳 400")
    void testCreateProductBadRequest() throws Exception {
        ProductCreateDTO productCreateDTO = ProductTestDataFactory.getProductCreateDTO(SeedProductData.MEAT_PRODUCT);
        productCreateDTO.setPrice(0);

        String jsonBody = objectMapper.writeValueAsString(productCreateDTO);
        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isBadRequest());

        verify(productService, never()).create(any(ProductCreateDTO.class));
    }


    @Test
    @DisplayName("[Unit] ProductController.pageQueryProduct() - 分頁查詢成功，應回傳 200 及資料")
    void testPageQueryProductSuccess() throws Exception {

        ProductPageQueryDTO queryDTO = new ProductPageQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setPageSize(5);

        List<ProductVO> mockData = new ArrayList<>();
        mockData.add(ProductTestDataFactory.getProductVO(SeedProductData.MEAT_PRODUCT));
        mockData.add(ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT));

        Long total = (long) mockData.size();
        PageResult mockResult = new PageResult(total, queryDTO.getPage(),
                queryDTO.getPageSize(), mockData);

        when(productService.pageQuery(any(ProductPageQueryDTO.class))).thenReturn(mockResult);

        ResultActions resultActions = mockMvc.perform(
                get("/products/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", queryDTO.getPage().toString())
                        .param("pageSize", queryDTO.getPageSize().toString())
                        .param("title", SeedProductData.MEAT_PRODUCT.title())
                        .param("productType", String.valueOf(SeedProductData.MEAT_PRODUCT.productType()))
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());

        verify(productService).pageQuery(any(ProductPageQueryDTO.class));
    }

    @Test
    @DisplayName("[Unit] ProductController.pageQueryProduct() - 分頁查詢參數錯誤，應回傳 400")
    void testPageQueryProductBadRequest() throws Exception {
        ProductPageQueryDTO queryDTO = new ProductPageQueryDTO();
        queryDTO.setPage(0);
        queryDTO.setPageSize(5);

        mockMvc.perform(
                        get("/products/page")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("page", queryDTO.getPage().toString())
                                .param("pageSize", queryDTO.getPageSize().toString())
                ).andExpect(status().isBadRequest());

        verify(productService, never()).pageQuery(any(ProductPageQueryDTO.class));
    }

    @Test
    @DisplayName("[Unit] ProductController.updateProductById() - 修改成功應回傳 200 及資料")
    void testUpdateProductByIdSuccess() throws Exception {

        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.MEAT_PRODUCT);

        ProductVO productVO = ProductTestDataFactory.getProductVO(SeedProductData.MEAT_PRODUCT);

        when(productService.updateById(any(ProductEditDTO.class))).thenReturn(productVO);

        String jsonBody = objectMapper.writeValueAsString(productEditDTO);
        mockMvc.perform(
                        put("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());

        verify(productService).updateById(any(ProductEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] ProductController.updateProductById() - 修改 id 不存在，應回傳 404 及指定訊息")
    void testUpdateProductByIdNoExist() throws Exception {

        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.MEAT_PRODUCT);


        doThrow(new ResourceNotFoundException(MessageEnum.PRODUCT_NOT_EXIST))
                .when(productService).updateById(any(ProductEditDTO.class));

        String jsonBody = objectMapper.writeValueAsString(productEditDTO);

        mockMvc.perform(
                        put("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_NOT_EXIST.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.PRODUCT_NOT_EXIST.getMessage()));

        verify(productService).updateById(any(ProductEditDTO.class));

    }


    @Test
    @DisplayName("[Unit] ProductController.updateProductById() - 修改品項名稱已存在，應回傳 409 及指定訊息")
    void testUpdateProductByIdTitleDuplicate() throws Exception {

        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.MEAT_PRODUCT);

        doThrow(new AlreadyExistsException(MessageEnum.PRODUCT_ALREADY_EXISTS))
                .when(productService).updateById(any(ProductEditDTO.class));

        String jsonBody = objectMapper.writeValueAsString(productEditDTO);

        mockMvc.perform(
                        put("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.PRODUCT_ALREADY_EXISTS.getMessage()));

        verify(productService).updateById(any(ProductEditDTO.class));

    }

    @Test
    @DisplayName("[Unit] ProductController.updateProductById() - 修改參數錯誤，應回傳 400")
    void testUpdateProductByIdBadRequest() throws Exception {
        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.MEAT_PRODUCT);
        productEditDTO.setProductType(ProductTypeEnum.values().length);

        String jsonBody = objectMapper.writeValueAsString(productEditDTO);
        mockMvc.perform(
                        put("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isBadRequest());

        verify(productService, never()).updateById(any(ProductEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] ProductController.getProductById() - id 不存在，應回傳 404 及指定訊息")
    void testGetByIdProductNotExist() throws Exception {

        Integer id = Integer.MAX_VALUE;

        doThrow(new ResourceNotFoundException(MessageEnum.PRODUCT_NOT_EXIST))
                .when(productService).getById(any(Integer.class));

        mockMvc.perform(
                        get("/products/{id}", id)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_NOT_EXIST.getCode()));

        verify(productService).getById(id);

    }

    @Test
    @DisplayName("[Unit] ProductController.getProductById() - id 存在，應回傳 200 及資料")
    void testGetByIdProductExist() throws Exception {

        Integer id = SeedProductData.DRINK_PRODUCT.id();
        ProductVO productVO = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        when(productService.getById(any(Integer.class))).thenReturn(productVO);

        mockMvc.perform(
                        get("/products/{id}", id)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());

        verify(productService).getById(id);
    }

//    @Test
//    @DisplayName("[Unit] ProductController.getActiveProductByType() - 查詢成功，應回傳 200 及資料")
//    void testGetActiveProductByTypeSuccess() throws Exception {
//
//        Integer productType = SeedProductData.MEAT_PRODUCT.productType();
//        List<ProductVO> mockData = new ArrayList<>();
//        mockData.add(ProductTestDataFactory.getProductVO(SeedProductData.MEAT_PRODUCT));
//        mockData.add(ProductTestDataFactory.getProductVO(SeedProductData.VEG_PRODUCT));
//
//        when(productService.getActiveProductByType(any())).thenReturn(mockData);
//
//        mockMvc.perform(
//                        get("/products/active")
//                                .param("productType", String.valueOf(productType))
//                ).andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
//                .andExpect(jsonPath("$.data").exists());
//
//        verify(productService).getActiveProductByType(any(ProductActiveQueryDTO.class));
//
//    }
//
//    @Test
//    @DisplayName("[Unit] ProductController.getActiveProductByType() - 查詢參數超出範圍，應回傳 400")
//    void testGetActiveProductByTypeBadRequest() throws Exception {
//
//        Integer productType = 3;
//
//        mockMvc.perform(
//                get("/products/active")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .param("productType", String.valueOf(productType))
//        ).andExpect(status().isBadRequest());
//
//        verify(productService, never()).getActiveProductByType(any(ProductActiveQueryDTO.class));
//    }


    @Test
    @DisplayName("[Unit] ProductController.updateProductStatus() - 更新成功，應回傳 200")
    void testUpdateProductStatusSuccess() throws Exception{
        Integer id = SeedProductData.DRINK_PRODUCT.id();
        ProductStatusDTO productStatusDTO = new ProductStatusDTO();
        productStatusDTO.setStatus(StatusEnum.ACTIVE.getCode());

        ProductVO mockProduct = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        when(productService.getById(any(Integer.class))).thenReturn(mockProduct);

        String jsonBody = objectMapper.writeValueAsString(productStatusDTO);
        mockMvc.perform(
                patch("/products/{id}/status",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isOk());

        verify(productService).updateStatus(anyInt(),any(ProductStatusDTO.class));
    }

    @Test
    @DisplayName("[Unit] ProductController.updateProductStatus() - id 不存在，應回傳 404 及指定訊息")
    void testUpdateProductStatusProductNotExist() throws Exception{
        Integer id = SeedProductData.DRINK_PRODUCT.id();
        ProductStatusDTO productStatusDTO = new ProductStatusDTO();
        productStatusDTO.setStatus(StatusEnum.ACTIVE.getCode());

        doThrow(new ResourceNotFoundException(MessageEnum.PRODUCT_NOT_EXIST))
                .when(productService).updateStatus(anyInt(),any(ProductStatusDTO.class));

        String jsonBody = objectMapper.writeValueAsString(productStatusDTO);
        mockMvc.perform(
                patch("/products/{id}/status",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_NOT_EXIST.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.PRODUCT_NOT_EXIST.getMessage()));
    }

    @Test
    @DisplayName("[Unit] ProductController.updateProductStatus() - 狀態參數超出範圍，應回傳 400")
    void testUpdateProductStatusInvalidStatus() throws Exception{

        Integer id = SeedProductData.DRINK_PRODUCT.id();
        ProductStatusDTO productStatusDTO = new ProductStatusDTO();
        productStatusDTO.setStatus(3);

        String jsonBody = objectMapper.writeValueAsString(productStatusDTO);
        mockMvc.perform(
                patch("/products/{id}/status",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isBadRequest());

        verify(productService,never()).updateStatus(anyInt(),any(ProductStatusDTO.class));
    }

}
