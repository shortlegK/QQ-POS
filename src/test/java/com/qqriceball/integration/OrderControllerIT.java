package com.qqriceball.integration;



import com.jayway.jsonpath.JsonPath;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OrderStatusEnum;
import com.qqriceball.model.dto.order.*;
import com.qqriceball.testData.order.SeedOrderData;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.utils.order.OrderTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerIT extends BaseIntegrationTest {


    @Test
    @DisplayName("[IT] 5001 createOrder - 使用管理權限帳號，建立訂單成功，回傳 200 及資料")
    void testCreateOrderWithManagerSuccess() throws Exception {

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> itemOptionDTOList = OrderTestDataFactory.getOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, itemOptionDTOList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        LocalDateTime expectedPickupTime = LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.MINUTES);
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
                .andExpect(jsonPath("$.data.pickupTime").value(expectedPickupTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
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
    @DisplayName("[IT] 5001 createOrder - 使用一般權限帳號，建立訂單成功，回傳 200 及資料")
    void testCreateOrderWithStaffSuccess() throws Exception {

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> itemOptions0 = OrderTestDataFactory.getOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);
        List<OrderItemOptionDTO> itemOptions1 = OrderTestDataFactory.getOptionDTOList(OrderTestDataFactory.DRINK_OPTIONS, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItem0 = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, itemOptions0);
        OrderItemDTO orderItem1 = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, itemOptions1);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        LocalDateTime expectedPickupTime = LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.MINUTES);
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
                .andExpect(jsonPath("$.data.pickupTime").value(expectedPickupTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
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
        List<OrderItemOptionDTO> itemOptionDTOList = OrderTestDataFactory.getOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, itemOptionDTOList);
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
        List<OrderItemOptionDTO> itemOptionDTOList = OrderTestDataFactory.getOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);
        itemOptionDTOList.get(0).setOptionId(Integer.MAX_VALUE);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, itemOptionDTOList);

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
        List<OrderItemOptionDTO> itemOptionDTOList = OrderTestDataFactory.getOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_INACTIVE, productQuantity, itemOptionDTOList);

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
        List<OrderItemOptionDTO> itemOptionDTOList = OrderTestDataFactory.getOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_INACTIVE, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, itemOptionDTOList);

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

    @Test
    @DisplayName("[IT] 5002 pageQueryOrder - 分頁查詢指定日期訂單成功，回傳 200 及資料")
    void testPageQueryOrderByDateSuccess() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();
        String orderNoPrefix = startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        mockMvc.perform(
                        get("/orders/page")
                                .header("Authorization", "Bearer " + tokenManager)
                                .param("startDate", startDate.toString())
                                .param("endDate", endDate.toString())
                                .param("page", "1")
                                .param("pageSize", "10")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isNotEmpty())
                .andExpect(jsonPath("$.data.records[*].orderNo").value(everyItem(startsWith(orderNoPrefix))))
                .andExpect(jsonPath("$.data.records[*].items").isArray())
                .andExpect(jsonPath("$.data.records[*].items").isNotEmpty())
                .andExpect(jsonPath("$.data.records[*].items[*].options").isArray())
                .andExpect(jsonPath("$.data.records[*].items[*].options").isNotEmpty());
    }

    @Test
    @DisplayName("[IT] 5002 pageQueryOrder - 分頁查詢指定狀態訂單成功，回傳 200 及資料")
    void testPageQueryOrderByStatusSuccess() throws Exception {

        mockMvc.perform(
                        get("/orders/page")
                                .header("Authorization", "Bearer " + tokenManager)
                                .param("status", OrderStatusEnum.MAKING.getCode()+"")
                                .param("startDate", LocalDate.now().toString())
                                .param("endDate", LocalDate.now().toString())
                                .param("page", "1")
                                .param("pageSize", "10")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isNotEmpty())
                .andExpect(jsonPath("$.data.records[*].status").value(everyItem(equalTo(OrderStatusEnum.MAKING.getCode()))))
                .andExpect(jsonPath("$.data.records[*].items").isArray())
                .andExpect(jsonPath("$.data.records[*].items").isNotEmpty())
                .andExpect(jsonPath("$.data.records[*].items[*].options").isArray())
                .andExpect(jsonPath("$.data.records[*].items[*].options").isNotEmpty());
    }

    @Test
    @DisplayName("[IT] 5002 pageQueryOrder - 分頁查詢指定訂單編號成功，應回傳 200 及資料")
    void testPageQueryOrderByOrderNoSuccess() throws Exception {
        String orderNo = SeedOrderData.orderMaking.orderNo();

        mockMvc.perform(
                        get("/orders/page")
                                .header("Authorization", "Bearer " + tokenManager)
                                .param("orderNo", orderNo)
                                .param("startDate", LocalDate.now().toString())
                                .param("endDate", LocalDate.now().toString())
                                .param("page", "1")
                                .param("pageSize", "10")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.size()").value(1))
                .andExpect(jsonPath("$.data.records[0].orderNo").value(orderNo))
                .andExpect(jsonPath("$.data.records[0].status").value(OrderStatusEnum.MAKING.getCode()))
                .andExpect(jsonPath("$.data.records[0].items").isArray())
                .andExpect(jsonPath("$.data.records[0].items").isNotEmpty())
                .andExpect(jsonPath("$.data.records[0].items[*].options").isArray())
                .andExpect(jsonPath("$.data.records[0].items[*].options").isNotEmpty());
    }

    @Test
    @DisplayName("[IT] 5003 updateOrderByOrderNo - 更新訂單資料，成功修改訂單，回傳 200 及資料")
    void testUpdateOrderByOrderNoSuccess() throws Exception {
        String expectedOrderNo = SeedOrderData.orderMaking.orderNo();

        OrderEditDTO orderEditDTO = new OrderEditDTO();
        LocalDateTime expectedPickupTime = LocalDateTime.now().plusMinutes(30).truncatedTo(ChronoUnit.MINUTES);
        orderEditDTO.setPickupTime(expectedPickupTime);
        orderEditDTO.setOrderNo(expectedOrderNo);

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> itemOptionDTOList = OrderTestDataFactory.getOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);
        Integer productQuantity = 4;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, itemOptionDTOList);
        orderEditDTO.setItems(List.of(orderItemDTO));

        Integer expectedTotal = OrderTestDataFactory.calculateTotalPrice(SeedProductData.MEAT_PRODUCT, productQuantity,
                OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);

        String jsonBody = objectMapper.writeValueAsString(orderEditDTO);
        mockMvc.perform(
                        put("/orders")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.orderNo").value(expectedOrderNo))
                .andExpect(jsonPath("$.data.pickupTime").value(expectedPickupTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.data.status").value(OrderStatusEnum.MAKING.getCode()))
                .andExpect(jsonPath("$.data.total").value(expectedTotal));
    }

    @Test
    @DisplayName("[IT] 5003 updateOrderByOrderNo - 更新訂單資料，查無訂單，應回傳 404")
    void testUpdateOrderByOrderNoNotFound() throws Exception {
        OrderEditDTO orderEditDTO = new OrderEditDTO();
        LocalDateTime expectedPickupTime = LocalDateTime.now().plusMinutes(30).truncatedTo(ChronoUnit.MINUTES);
        orderEditDTO.setPickupTime(expectedPickupTime);
        orderEditDTO.setOrderNo("NotExistOrderNo");

        List<OrderItemOptionDTO> itemOptionDTOList = OrderTestDataFactory.getOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, 1);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, 1, itemOptionDTOList);
        orderEditDTO.setItems(List.of(orderItemDTO));

        String jsonBody = objectMapper.writeValueAsString(orderEditDTO);
        mockMvc.perform(
                        put("/orders")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ORDER_NOT_EXIST.getCode()));
    }

    @Test
    @DisplayName("[IT] 5003 updateOrderByOrderNo - 更新訂單資料，訂單狀態非製作中，應回傳 409")
    void testUpdateOrderByOrderNoInvalidStatus() throws Exception {
        String orderNo = SeedOrderData.orderReady.orderNo();
        OrderEditDTO orderEditDTO = new OrderEditDTO();
        LocalDateTime expectedPickupTime = LocalDateTime.now().plusMinutes(30).truncatedTo(ChronoUnit.MINUTES);
        orderEditDTO.setPickupTime(expectedPickupTime);
        orderEditDTO.setOrderNo(orderNo);

        List<OrderItemOptionDTO> itemOptionDTOList = OrderTestDataFactory.getOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, 1);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, 1, itemOptionDTOList);
        orderEditDTO.setItems(List.of(orderItemDTO));

        String jsonBody = objectMapper.writeValueAsString(orderEditDTO);
        mockMvc.perform(
                        put("/orders")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.ORDER_NOT_EDITABLE.getCode()));
    }

    @Test
    @DisplayName("[IT] 5004 getOrderByOrderNo - 根據 OrderNo 查詢訂單資料成功，回傳 200 及資料")
    void testGetOrderByOrderNoSuccess() throws Exception {
        String orderNo = SeedOrderData.orderReady.orderNo();

        LocalDateTime expectedPickupTime = SeedOrderData.orderReady.pickupTime();
        Integer expectedTotal = SeedOrderData.orderReady.total();
        Integer expectedStatus = SeedOrderData.orderReady.status();

        mockMvc.perform(
                        get("/orders/{orderNo}", orderNo)
                                .header("Authorization", "Bearer " + tokenManager)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.orderNo").value(orderNo))
                .andExpect(jsonPath("$.data.pickupTime").value(expectedPickupTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.data.total").value(expectedTotal))
                .andExpect(jsonPath("$.data.status").value(expectedStatus))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isNotEmpty())
                .andExpect(jsonPath("$.data.items[*].options").isArray())
                .andExpect(jsonPath("$.data.items[*].options").isNotEmpty());
    }

    @Test
    @DisplayName("[IT] 5004 getOrderByOrderNo - 根據 OrderNo 查詢訂單資料，查無訂單，應回傳 404")
    void testGetOrderByOrderNoNotFound() throws Exception {
        String orderNo = "NotExistOrderNo";

        mockMvc.perform(
                get("/orders/{orderNo}", orderNo)
                        .header("Authorization", "Bearer " + tokenManager)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ORDER_NOT_EXIST.getCode()));
    }

    @Test
    @DisplayName("[IT] 5005 updateOrderStatusByOrderNo - 根據 OrderNo 更新訂單狀態成功，回傳 200")
    void testUpdateOrderStatusByOrderNoSuccess() throws Exception {
        String orderNo = SeedOrderData.orderReady.orderNo();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(OrderStatusEnum.PICKED_UP.getCode());

        String jsonBody = objectMapper.writeValueAsString(orderStatusDTO);
        mockMvc.perform(
                        patch("/orders/{orderNo}/status",orderNo)
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()));

        // 驗證訂單狀態已更新
        mockMvc.perform(
                        get("/orders/{orderNo}", orderNo)
                                .header("Authorization", "Bearer " + tokenManager)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(orderStatusDTO.getStatus()));
    }

    @Test
    @DisplayName("[IT] 5005 updateOrderStatusByOrderNo - 根據 OrderNo 更新訂單狀態，查無訂單，應回傳 404")
    void testUpdateOrderStatusByOrderNoNotFound() throws Exception {
        String orderNo = "NotExistOrderNo";
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(OrderStatusEnum.READY.getCode());

        String jsonBody = objectMapper.writeValueAsString(orderStatusDTO);
        mockMvc.perform(
                patch("/orders/{orderNo}/status",orderNo)
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ORDER_NOT_EXIST.getCode()));
    }

    @Test
    @DisplayName("[IT] 5005 updateOrderStatusByOrderNo - 根據 OrderNo 更新訂單狀態，狀態轉換不合法，應回傳 400")
    void testUpdateOrderStatusByOrderNoInvalidTransition() throws Exception {
        String orderNo = SeedOrderData.orderReady.orderNo();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(OrderStatusEnum.CANCELLED.getCode());

        String jsonBody = objectMapper.writeValueAsString(orderStatusDTO);
        mockMvc.perform(
                        patch("/orders/{orderNo}/status",orderNo)
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.ORDER_STATUS_TRANSITION_NOT_ALLOWED.getCode()));
    }
}
