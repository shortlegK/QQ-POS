package com.qqriceball.integration;



import com.jayway.jsonpath.JsonPath;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OrderStatusEnum;
import com.qqriceball.model.dto.order.OrderCreateDTO;
import com.qqriceball.model.dto.order.OrderItemDTO;
import com.qqriceball.model.dto.order.OrderItemOptionDTO;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.utils.order.OrderTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerIT extends BaseIntegrationTest {


    @Test
    @DisplayName("[IT] 5001 createOrder - 使用管理權限帳號，建立產品細節選項成功，回傳 200 及資料")
    void testCreateOrderWithManagerSuccess() throws Exception {

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> itemOptionsDTO = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, itemOptionsDTO);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        LocalDateTime expectedPickupTime = LocalDateTime.now();
        orderCreateDTO.setPickupTime(expectedPickupTime);
        orderCreateDTO.setItems(List.of(orderItemDTO));

        Integer expectedTotal = OrderTestDataFactory.calculateTotalPrice(SeedProductData.MEAT_PRODUCT, productQuantity,
                OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        MvcResult result = mockMvc.perform(
                        post("/orders/create")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.total").value(expectedTotal))
                .andExpect(jsonPath("$.data.pickupTime").value(expectedPickupTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))))
                .andExpect(jsonPath("$.data.status").value(OrderStatusEnum.MAKING.getCode()))
                .andReturn();

        // 驗證 orderNo 格式
        String orderNo = JsonPath.read(result.getResponse().getContentAsString(), "$.data.orderNo");
        String expectedDatePrefix = expectedPickupTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        assertAll(
                () -> assertEquals(12, orderNo.length(), "orderNo 長度應為 12 碼"),
                () -> assertTrue(orderNo.startsWith(expectedDatePrefix), "orderNo 前 8 碼應為預計取餐時間的日期 (yyyyMMdd)"),
                () -> assertTrue(orderNo.substring(8).matches("\\d{4}"), "orderNo 後 4 碼應為數字流水號")
        );
    }

    @Test
    @DisplayName("[IT] 5001 createOrder - 使用一般權限帳號，建立產品細節選項成功，回傳 200 及資料")
    void testCreateOrderWithStaffSuccess() throws Exception {

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> itemOptions0 = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);
        List<OrderItemOptionDTO> itemOptions1 = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.DRINK_OPTIONS, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItem0 = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, itemOptions0);
        OrderItemDTO orderItem1 = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, itemOptions1);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        LocalDateTime expectedPickupTime = LocalDateTime.now();
        orderCreateDTO.setPickupTime(expectedPickupTime);
        orderCreateDTO.setItems(List.of(orderItem0, orderItem1));

        Integer item0Total = OrderTestDataFactory.calculateTotalPrice(SeedProductData.MEAT_PRODUCT, productQuantity,
                OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);

        Integer item1Total = OrderTestDataFactory.calculateTotalPrice(SeedProductData.DRINK_PRODUCT, productQuantity,
                OrderTestDataFactory.DRINK_OPTIONS, optionQuantity);

        Integer expectedTotal = item0Total + item1Total;

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        MvcResult result = mockMvc.perform(
                        post("/orders/create")
                                .header("Authorization", "Bearer " + tokenStaff)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.total").value(expectedTotal))
                .andExpect(jsonPath("$.data.pickupTime").value(expectedPickupTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))))
                .andExpect(jsonPath("$.data.status").value(OrderStatusEnum.MAKING.getCode()))
                .andReturn();

        // 驗證 orderNo 格式
        String orderNo = JsonPath.read(result.getResponse().getContentAsString(), "$.data.orderNo");
        String expectedDatePrefix = expectedPickupTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        assertAll(
                () -> assertEquals(12, orderNo.length(), "orderNo 長度應為 12 碼"),
                () -> assertTrue(orderNo.startsWith(expectedDatePrefix), "orderNo 前 8 碼應為預計取餐時間的日期 (yyyyMMdd)"),
                () -> assertTrue(orderNo.substring(8).matches("\\d{4}"), "orderNo 後 4 碼應為數字流水號")
        );
    }

    @Test
    @DisplayName("[IT] 5001 createOrder - 建立訂單，查無產品資料，應回傳 404")
    void testCreateOrderProductNotFound() throws Exception {

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> itemOptionsDTO = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, itemOptionsDTO);
        orderItemDTO.setProductId(Integer.MAX_VALUE);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_NOT_EXIST.getCode()));
    }


    @Test
    @DisplayName("[IT] 5001 createOrder - 建立訂單，查無選項資料，應回傳 404")
    void testCreateOrderOptionNotFound() throws Exception {

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> itemOptionsDTO = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);
        itemOptionsDTO.get(0).setOptionId(Integer.MAX_VALUE);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, itemOptionsDTO);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_NOT_EXIST.getCode()));

    }

    @Test
    @DisplayName("[IT] 5001 createOrder - 建立訂單，產品已下架，應回傳 409")
    void testCreateOrderProductUnavailable() throws Exception {

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> itemOptionsDTO = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_INACTIVE, productQuantity, itemOptionsDTO);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        LocalDateTime expectedPickupTime = LocalDateTime.now();
        orderCreateDTO.setPickupTime(expectedPickupTime);
        orderCreateDTO.setItems(List.of(orderItemDTO));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_UNAVAILABLE.getCode()));

    }

    @Test
    @DisplayName("[IT] 5001 createOrder - 建立訂單，選項已下架，應回傳 409")
    void testCreateOrderOptionUnavailable() throws Exception {

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> itemOptionsDTO = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_INACTIVE, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, itemOptionsDTO);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_UNAVAILABLE.getCode()));

    }

}
