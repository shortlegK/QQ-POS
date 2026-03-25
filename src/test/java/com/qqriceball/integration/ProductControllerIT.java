package com.qqriceball.integration;

import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.model.dto.product.ProductCreateDTO;
import com.qqriceball.model.dto.product.ProductEditDTO;
import com.qqriceball.model.dto.product.ProductPageQueryDTO;
import com.qqriceball.model.dto.product.ProductStatusDTO;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.utils.TestDataGenerator;
import com.qqriceball.utils.product.ProductTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ProductControllerIT extends BaseIntegrationTest{

    @Test
    @DisplayName("[IT] 3001 createProduct - 建立品項成功，應回傳 200 及資料")
    void testCreateProductSuccess() throws Exception {

        ProductCreateDTO productCreateDTO = ProductTestDataFactory.getProductCreateDTO(SeedProductData.DRINK_PRODUCT);
        String productTile = TestDataGenerator.getUnique("create");
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

        ProductCreateDTO productCreateDTO = ProductTestDataFactory.getProductCreateDTO(SeedProductData.DRINK_PRODUCT);
        productCreateDTO.setTitle(TestDataGenerator.getUnique("duplicate"));


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

        mockMvc.perform(
                get("/products/page")
                        .header("Authorization", "Bearer " + tokenManager)
                        .param("page", queryDTO.getPage().toString())
                        .param("pageSize", queryDTO.getPageSize().toString())
                        .param("title", queryDTO.getTitle())
                ).andExpect(status().isOk())
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

        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.MEAT_PRODUCT);
        productEditDTO.setTitle(TestDataGenerator.getUnique("update"));

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

        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.MEAT_PRODUCT);
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

        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.MEAT_PRODUCT);
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

    @Test
    @DisplayName("[IT] 3004 getProductById - id 不存在，應回傳 404 及指定訊息")
    void testGetByIdProductNotExist() throws Exception {

        Integer id = Integer.MAX_VALUE;

        mockMvc.perform(
                        get("/products/{id}", id)
                                .header("Authorization", "Bearer " + tokenManager))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_NOT_EXIST.getCode()));
    }

    @Test
    @DisplayName("[IT] 3004 getProductById - id 存在，應回傳 200 及資料")
    void testGetByIdProductExist() throws Exception {
        Integer id = SeedProductData.VEG_PRODUCT.id();
        mockMvc.perform(
                        get("/products/{id}", id)
                                .header("Authorization", "Bearer " + tokenManager))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.title").value(SeedProductData.VEG_PRODUCT.title()));

    }

    @Test
    @DisplayName("[IT] 3005 getActiveProductByType - 查詢成功，應回傳 200 及資料")
    void testGetActiveProductByTypeSuccess() throws Exception {
        ProductTypeEnum productType = ProductTypeEnum.DRINKS;

        mockMvc.perform(
                        get("/products/active")
                                .header("Authorization", "Bearer " + tokenManager)
                                .param("productType", String.valueOf(productType.getCode()))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].productType").value(everyItem(equalTo(productType.getCode()))))
                .andExpect(jsonPath("$.data[*].status").value(everyItem(equalTo(StatusEnum.ACTIVE.getCode()))));
    }

    @Test
    @DisplayName("[IT] 3006 updateProductStatus - 更新成功，應回傳 200")
    void testUpdateProductStatusSuccess() throws Exception{
        Integer id = SeedProductData.DRINK_PRODUCT.id();
        ProductStatusDTO productStatusDTO = new ProductStatusDTO();
        productStatusDTO.setStatus(StatusEnum.ACTIVE.getCode());

        String jsonBody = objectMapper.writeValueAsString(productStatusDTO);
        mockMvc.perform(
                patch("/products/{id}/status",id)
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isOk());

        mockMvc.perform(
                get("/products/{id}", id)
                        .header("Authorization", "Bearer " + tokenManager))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.status").value(productStatusDTO.getStatus())
        );
    }

    @Test
    @DisplayName("[IT] 3006 updateProductStatus - 更新品項 id 不存在，應回傳 404")
    void testUpdateProductStatusNotExist() throws Exception{
        Integer id = Integer.MAX_VALUE;
        ProductStatusDTO productStatusDTO = new ProductStatusDTO();
        productStatusDTO.setStatus(StatusEnum.ACTIVE.getCode());

        String jsonBody = objectMapper.writeValueAsString(productStatusDTO);
        mockMvc.perform(
                        patch("/products/{id}/status",id)
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_NOT_EXIST.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.PRODUCT_NOT_EXIST.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

}
