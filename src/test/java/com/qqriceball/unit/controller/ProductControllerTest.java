package com.qqriceball.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.controller.ProductController;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.handler.GlobalExceptionHandler;
import com.qqriceball.integration.testData.emp.SeedUserData;
import com.qqriceball.integration.testData.product.SeedProductData;
import com.qqriceball.integration.testData.product.TestProduct;
import com.qqriceball.model.dto.ProductDTO;
import com.qqriceball.model.dto.ProductPageQueryDTO;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.model.vo.ProductPageQueryVO;
import com.qqriceball.model.vo.ProductVO;
import com.qqriceball.service.EmpService;
import com.qqriceball.service.ProductService;
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
    void setUpAuth(){
        EmpVO emp = new EmpVO();
        emp.setId(99);
        Authentication auth =  new UsernamePasswordAuthenticationToken(emp, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void cleanAuth(){
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("[Unit] ProductController.createProduct - 建立重複品項，應回傳 409 及指定訊息")
    void testCreateProductTitleDuplicate() throws Exception {

        ProductDTO productDTO = getProductDTO(SeedProductData.MEAT_PRODUCT);

        doThrow(new AlreadyExistsException(MessageEnum.PRODUCT_ALREADY_EXIST))
                .when(productService)
                .create(any(ProductDTO.class));

        String jsonBody = objectMapper.writeValueAsString(productDTO);
        mockMvc.perform(
                post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_ALREADY_EXIST.getCode()));
    }

    @Test
    @DisplayName("[Unit] ProductController.createProduct - 建立品項成功，應回傳 200")
    void testCreateProductSuccess() throws Exception {

        ProductDTO productDTO = getProductDTO(SeedProductData.MEAT_PRODUCT);

        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(productDTO, productVO);
        when(productService.create(any(ProductDTO.class))).thenReturn(productVO);

        String jsonBody = objectMapper.writeValueAsString(productDTO);
        mockMvc.perform(
                        post("/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());
    }


    @Test
    @DisplayName("[Unit] ProductController.pageQuery - 分頁查詢成功，應回傳 200 及資料")
    void testPageQueryProductSuccess() throws Exception {

        ProductPageQueryDTO queryDTO = new ProductPageQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setPageSize(5);

        List<ProductPageQueryVO> mockData = new ArrayList<>();
        mockData.add(getProductPageQueryVO(SeedProductData.MEAT_PRODUCT));
        mockData.add(getProductPageQueryVO(SeedProductData.DRINK_PRODUCT));

        Long total = (long) mockData.size();
        PageResult mockResult = new PageResult(total, queryDTO.getPage(),
                queryDTO.getPageSize(), mockData);

        when(productService.pageQuery(any(ProductPageQueryDTO.class))).thenReturn(mockResult);

        ResultActions resultActions = mockMvc.perform(
                get("/product/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", queryDTO.getPage().toString())
                        .param("pageSize", queryDTO.getPageSize().toString())
                        .param("title", SeedProductData.MEAT_PRODUCT.title())
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());

        verify(productService).pageQuery(any(ProductPageQueryDTO.class));
    }

    private static ProductDTO getProductDTO(TestProduct product){
        ProductDTO productDTO = new ProductDTO();
        BeanUtils.copyProperties(product, productDTO);
        return productDTO;
    }

    private static  ProductPageQueryVO getProductPageQueryVO(TestProduct product){
        ProductPageQueryVO productPageQueryVO = new ProductPageQueryVO();
        BeanUtils.copyProperties(product, productPageQueryVO);
        return productPageQueryVO;
    }

}
