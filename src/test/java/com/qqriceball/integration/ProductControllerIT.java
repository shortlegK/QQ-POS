package com.qqriceball.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.integration.testData.emp.SeedUserData;
import com.qqriceball.integration.testData.product.SeedProductData;
import com.qqriceball.integration.testData.product.TestProduct;
import com.qqriceball.integration.utils.Utils;
import com.qqriceball.model.dto.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenManager;
    private String tokenStaff;

    @BeforeAll
    void setUp() throws Exception {

        // 取得 Admin Token
        EmpLoginDTO managerLoginDTO = getEmpLoginDTO(
                SeedUserData.MANAGER.username(), SeedUserData.MANAGER.password());

        String jsonBody = objectMapper.writeValueAsString(managerLoginDTO);
        MvcResult managerResult = mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn();

        tokenManager = JsonPath.read(managerResult.getResponse().getContentAsString(),"$.data.token");

        // 取得 Staff Token
        EmpLoginDTO staffLoginDTO = getEmpLoginDTO(
                SeedUserData.STAFF.username(), SeedUserData.STAFF.password());

        jsonBody = objectMapper.writeValueAsString(staffLoginDTO);
        MvcResult staffResult = mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn();

        tokenStaff = JsonPath.read(staffResult.getResponse().getContentAsString(),"$.data.token");

        assertAll(
                () -> assertFalse(tokenManager.isBlank()),
                () -> assertFalse(tokenStaff.isBlank())
        );
    }

    @Test
    @DisplayName("[IT] 3001 createProduct - 建立品項成功，應回傳 200 及資料")
    void testCreateProductSuccess() throws Exception {

        ProductCreateDTO productCreateDTO = getProductDTO(SeedProductData.DRINK_PRODUCT);
        String productTile = Utils.getUnique("create");
        productCreateDTO.setTitle(productTile);

        String jsonBody = objectMapper.writeValueAsString(productCreateDTO);
        mockMvc.perform(
                post("/products")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.title").value(productTile))
                .andExpect(jsonPath("$.data.productType").value(ProductTypeEnum.DRINKS.getCode()))
                .andExpect(jsonPath("$.data.price").value(productCreateDTO.getPrice()))
                .andExpect(jsonPath("$.data.status").value(productCreateDTO.getStatus()));
    }

    @Test
    @DisplayName("[IT] 3001 createProduct - 建立重複品項，應回傳 409 及指定訊息")
    void testCreateProductTitleDuplicate() throws Exception {

        ProductCreateDTO productCreateDTO = getProductDTO(SeedProductData.DRINK_PRODUCT);
        productCreateDTO.setTitle(Utils.getUnique("duplicate"));


        String jsonBody = objectMapper.writeValueAsString(productCreateDTO);

        mockMvc.perform(
                post("/products")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isOk());

        mockMvc.perform(
                        post("/products")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.PRODUCT_ALREADY_EXISTS.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    @DisplayName("[IT] 3002 pageQueryProduct - 分頁查詢成功，應回傳 200 及資料")
    void testPageQueryProductSuccess() throws Exception {

        ProductPageQueryDTO queryDTO = new ProductPageQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setPageSize(5);
        queryDTO.setTitle(SeedProductData.MEAT_PRODUCT.title());

        ResultActions resultActions = mockMvc.perform(
                get("/products/page")
                        .header("Authorization", "Bearer " + tokenManager)
                        .param("page", queryDTO.getPage().toString())
                        .param("pageSize", queryDTO.getPageSize().toString())
                        .param("title", queryDTO.getTitle())
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.page").value(queryDTO.getPage()))
                .andExpect(jsonPath("$.data.pageSize").value(queryDTO.getPageSize()))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isNotEmpty())
                .andExpect(jsonPath("$.data.records[0].id").value(SeedProductData.MEAT_PRODUCT.id()))
                .andExpect(jsonPath("$.data.records[0].title").value(SeedProductData.MEAT_PRODUCT.title()));
    }

    @Test
    @DisplayName("[IT] 3003 updateProductById - 修改成功應回傳 200 及資料")
    void testUpdateProductByIdSuccess() throws Exception {

        ProductEditDTO productEditDTO = getProductEditDTO(SeedProductData.MEAT_PRODUCT);
        productEditDTO.setTitle(Utils.getUnique("update"));

        String jsonBody = objectMapper.writeValueAsString(productEditDTO);
        mockMvc.perform(
                put("/products")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(productEditDTO.getId()))
                .andExpect(jsonPath("$.data.title").value(productEditDTO.getTitle()));
    }

    @Test
    @DisplayName("[IT] 3003 updateProductById - 修改品項 id 不存在，應回傳 404")
    void testUpdateProductByIdNoExist() throws Exception{

        ProductEditDTO productEditDTO = getProductEditDTO(SeedProductData.MEAT_PRODUCT);
        productEditDTO.setId(Integer.MAX_VALUE);

        String jsonBody = objectMapper.writeValueAsString(productEditDTO);
        mockMvc.perform(
                        put("/products")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_NOT_EXIST.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.PRODUCT_NOT_EXIST.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[IT] 3003 updateProductById - 修改品項名稱已存在，應回傳 409 及指定訊息")
    void testUpdateProductByIdTitleDuplicate() throws Exception {

        ProductEditDTO productEditDTO = getProductEditDTO(SeedProductData.MEAT_PRODUCT);
        productEditDTO.setTitle(SeedProductData.VEG_PRODUCT.title());

        String jsonBody = objectMapper.writeValueAsString(productEditDTO);
        mockMvc.perform(
                        put("/products")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.PRODUCT_ALREADY_EXISTS.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    private static ProductCreateDTO getProductDTO(TestProduct product){
        ProductCreateDTO productCreateDTO = new ProductCreateDTO();
        BeanUtils.copyProperties(product, productCreateDTO);
        return productCreateDTO;
    }

    private static EmpLoginDTO getEmpLoginDTO(String username, String password) {
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(password);

        return empLoginDTO;
    }

    private static ProductEditDTO getProductEditDTO(TestProduct product){
            ProductEditDTO productEditDTO = new ProductEditDTO();
            BeanUtils.copyProperties(product, productEditDTO);
            return productEditDTO;

        }
}
