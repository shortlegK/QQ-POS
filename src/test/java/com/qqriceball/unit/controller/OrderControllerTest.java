package com.qqriceball.unit.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.exception.ResourceNotFoundException;
import com.qqriceball.common.exception.ResourceUnavailableException;
import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.controller.OrderController;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OrderStatusEnum;
import com.qqriceball.handler.GlobalExceptionHandler;
import com.qqriceball.model.dto.order.*;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.model.vo.order.OrderDetailVO;
import com.qqriceball.model.vo.order.OrderSummaryVO;
import com.qqriceball.model.vo.order.catalog.OrderCatalogVO;
import com.qqriceball.service.EmpService;
import com.qqriceball.service.OrderService;
import com.qqriceball.testData.order.SeedOrderData;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.utils.order.OrderTestDataFactory;
import org.junit.jupiter.api.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

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
    @DisplayName("[Unit] OrderController.createOrder() - 建立訂單成功，應回傳 200 及資料")
    void testCreateOrderSuccess() throws Exception {

        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdsList);
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        OrderSummaryVO orderSummaryVO = new OrderSummaryVO();
        BeanUtils.copyProperties(SeedOrderData.orderMaking, orderSummaryVO);
        when(orderService.create(any(OrderCreateDTO.class))).thenReturn(orderSummaryVO);

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());

        verify(orderService).create(any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.createOrder() - 建立訂單缺少 items ，應回傳 400")
    void testCreateOrderMissingItems() throws Exception {

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                post("/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isBadRequest());

        verify(orderService, never()).create(any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.createOrder() - 建立訂單缺少 options ，應回傳 400")
    void testCreateOrderMissingOptions() throws Exception {

        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, List.of());

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                post("/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isBadRequest());

        verify(orderService, never()).create(any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.createOrder() - 建立訂單，查無產品資料應回傳 404 及指定訊息")
    void testCreateProductNotExist() throws Exception {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 1, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        doThrow(new ResourceNotFoundException(MessageEnum.PRODUCT_NOT_EXIST))
                .when(orderService).create(any(OrderCreateDTO.class));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_NOT_EXIST.getCode()));

        verify(orderService).create(any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.createOrder() - 建立訂單，查無選項資料應回傳 404 及指定訊息")
    void testCreateOptionNotExist() throws Exception {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 1, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        doThrow(new ResourceNotFoundException(MessageEnum.OPTION_NOT_EXIST))
                .when(orderService).create(any(OrderCreateDTO.class));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_NOT_EXIST.getCode()));

        verify(orderService).create(any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.createOrder() - 建立訂單，產品為下架狀態應回傳 409 及指定訊息")
    void testCreateProductInactive() throws Exception {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 1, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        doThrow(new ResourceUnavailableException(MessageEnum.PRODUCT_UNAVAILABLE))
                .when(orderService).create(any(OrderCreateDTO.class));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.PRODUCT_UNAVAILABLE.getCode()));

        verify(orderService).create(any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.createOrder() - 建立訂單，選項為下架狀態應回傳 409 及指定訊息")
    void testCreateOptionInactive() throws Exception {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 1, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        doThrow(new ResourceUnavailableException(MessageEnum.OPTION_UNAVAILABLE))
                .when(orderService).create(any(OrderCreateDTO.class));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_UNAVAILABLE.getCode()));

        verify(orderService).create(any(OrderCreateDTO.class));
    }


    @Test
    @DisplayName("[Unit] OrderController.createOrder() - 建立訂單，產品設有無法使用的選項應回傳 400 及指定訊息")
    void testCreateOptionNotAllowed() throws Exception {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 1, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        doThrow(new BadRequestArgsException(MessageEnum.OPTION_TYPE_NOT_ALLOWED))
                .when(orderService).create(any(OrderCreateDTO.class));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_TYPE_NOT_ALLOWED.getCode()));

        verify(orderService).create(any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.createOrder() - 建立訂單，產品重複設定單選選項應回傳 400 及指定訊息")
    void testCreateSingleSelectOptionDuplicate() throws Exception {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 1, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        doThrow(new BadRequestArgsException(MessageEnum.DUPLICATE_OPTION))
                .when(orderService).create(any(OrderCreateDTO.class));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.DUPLICATE_OPTION.getCode()));

        verify(orderService).create(any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.createOrder() - 建立訂單，缺少必填選項，應回傳 400 及指定訊息")
    void testCreateRequiredOptionMissing() throws Exception {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 1, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        doThrow(new BadRequestArgsException(MessageEnum.REQUIRED_OPTION_MISSING))
                .when(orderService).create(any(OrderCreateDTO.class));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        mockMvc.perform(
                        post("/orders/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.REQUIRED_OPTION_MISSING.getCode()));

        verify(orderService).create(any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.pageQueryOrder() - 查詢訂單列表成功，應回傳 200 及資料")
    void testPageQueryOrderSuccess() throws Exception {

        Integer page = 1;
        Integer pageSize = 2;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(2);

        OrderPageQueryDTO orderPageQueryDTO = OrderTestDataFactory.getOrderPageQueryDTO(page, pageSize,
                null, null, startDate, endDate);

        List<OrderDetailVO> mockData = new ArrayList<>();
        mockData.add(OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderMaking,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON));

        Long total = 1L;
        PageResult mockResult = new PageResult(total, orderPageQueryDTO.getPage(),
                orderPageQueryDTO.getPageSize(), mockData);

        when(orderService.pageQuery(any(OrderPageQueryDTO.class))).thenReturn(mockResult);

        mockMvc.perform(
                        get("/orders/page")
                                .param("page", orderPageQueryDTO.getPage().toString())
                                .param("pageSize", orderPageQueryDTO.getPageSize().toString())
                                .param("startDate", orderPageQueryDTO.getStartDate().toString())
                                .param("endDate", orderPageQueryDTO.getEndDate().toString())
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());
        verify(orderService).pageQuery(any(OrderPageQueryDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.pageQueryOrder() - 查詢訂單列表，狀態參數超出範圍應回傳 400")
    void testPageQueryOrderInvalidStatus() throws Exception {

        Integer page = 1;
        Integer pageSize = 2;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(2);
        Integer invalidStatus = 5;

        OrderPageQueryDTO orderPageQueryDTO = OrderTestDataFactory.getOrderPageQueryDTO(page, pageSize,
                null, invalidStatus, startDate, endDate);

        mockMvc.perform(
                get("/orders/page")
                        .param("page", orderPageQueryDTO.getPage().toString())
                        .param("pageSize", orderPageQueryDTO.getPageSize().toString())
                        .param("status", orderPageQueryDTO.getStatus().toString())
                        .param("startDate", orderPageQueryDTO.getStartDate().toString())
                        .param("endDate", orderPageQueryDTO.getEndDate().toString())
        ).andExpect(status().isBadRequest());

        verify(orderService, never()).pageQuery(any(OrderPageQueryDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.pageQueryOrder() - 查詢訂單列表，缺少必填日期參數應回傳 400")
    void testPageQueryOrderMissingDate() throws Exception {

        Integer page = 1;
        Integer pageSize = 2;
        LocalDate startDate = LocalDate.now();


        OrderPageQueryDTO orderPageQueryDTO = OrderTestDataFactory.getOrderPageQueryDTO(page, pageSize,
                null, null, startDate, null);

        mockMvc.perform(
                get("/orders/page")
                        .param("page", orderPageQueryDTO.getPage().toString())
                        .param("pageSize", orderPageQueryDTO.getPageSize().toString())
                        .param("startDate", orderPageQueryDTO.getStartDate().toString())
        ).andExpect(status().isBadRequest());

        verify(orderService, never()).pageQuery(any(OrderPageQueryDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.updateByOrderNo() - 更新訂單資料成功，應回傳 200 及資料")
    void testUpdateOrderByOrderNoSuccess() throws Exception {

        List<Integer> optionIdList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdList);
        OrderEditDTO orderEditDTO = new OrderEditDTO();
        orderEditDTO.setPickupTime(LocalDateTime.now());
        orderEditDTO.setItems(List.of(orderItemDTO));
        orderEditDTO.setOrderNo(SeedOrderData.orderMaking.orderNo());

        OrderSummaryVO orderSummaryVO = new OrderSummaryVO();
        BeanUtils.copyProperties(SeedOrderData.orderMaking, orderSummaryVO);
        when(orderService.updateByOrderNo(any(OrderEditDTO.class))).thenReturn(orderSummaryVO);

        String jsonBody = objectMapper.writeValueAsString(orderEditDTO);
        mockMvc.perform(
                        put("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());
        verify(orderService).updateByOrderNo(any(OrderEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.updateByOrderNo() - 更新訂單資料缺少訂單編號，應回傳 400 及指定訊息")
    void testUpdateOrderByOrderNoWithOutOrderNo() throws Exception {

        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdsList);
        OrderEditDTO orderEditDTO = new OrderEditDTO();
        orderEditDTO.setPickupTime(LocalDateTime.now());
        orderEditDTO.setItems(List.of(orderItemDTO));

        String jsonBody = objectMapper.writeValueAsString(orderEditDTO);
        mockMvc.perform(
                        put("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.BAD_REQUEST.getCode()));

        verify(orderService, never()).updateByOrderNo(any(OrderEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.updateByOrderNo() - 更新訂單資料，查無訂單應回傳 404 及指定訊息")
    void testUpdateOrderByOrderNoNotExist() throws Exception {

        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdsList);
        OrderEditDTO orderEditDTO = new OrderEditDTO();
        orderEditDTO.setPickupTime(LocalDateTime.now());
        orderEditDTO.setItems(List.of(orderItemDTO));
        orderEditDTO.setOrderNo(SeedOrderData.orderMaking.orderNo());

        doThrow(new ResourceNotFoundException(MessageEnum.ORDER_NOT_EXIST))
                .when(orderService).updateByOrderNo(any(OrderEditDTO.class));

        String jsonBody = objectMapper.writeValueAsString(orderEditDTO);
        mockMvc.perform(
                        put("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ORDER_NOT_EXIST.getCode()));

        verify(orderService).updateByOrderNo(any(OrderEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.updateByOrderNo() - 更新訂單資料，訂單狀態非製作中應回傳 409 及指定訊息")
    void testUpdateOrderByOrderNoStatusError() throws Exception {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdsList);
        OrderEditDTO orderEditDTO = new OrderEditDTO();
        orderEditDTO.setPickupTime(LocalDateTime.now());
        orderEditDTO.setItems(List.of(orderItemDTO));
        orderEditDTO.setOrderNo(SeedOrderData.orderCancel.orderNo());

        doThrow(new ResourceUnavailableException(MessageEnum.ORDER_NOT_EDITABLE))
                .when(orderService).updateByOrderNo(any(OrderEditDTO.class));

        String jsonBody = objectMapper.writeValueAsString(orderEditDTO);
        mockMvc.perform(
                        put("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.ORDER_NOT_EDITABLE.getCode()));

        verify(orderService).updateByOrderNo(any(OrderEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.getByOrderNo() - 查詢訂單資料成功，應回傳 200 及資料")
    void testGetOrderByOrderNoSuccess() throws Exception {
        String expectedOrderNo = SeedOrderData.orderMaking.orderNo();

        OrderDetailVO mockOrderDetail = OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderMaking,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON);

        when(orderService.getByOrderNo(anyString())).thenReturn(mockOrderDetail);

        mockMvc.perform(
                        get("/orders/{orderNo}", expectedOrderNo)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());
        verify(orderService).getByOrderNo(anyString());
    }

    @Test
    @DisplayName("[Unit] OrderController.getByOrderNo() - 查詢訂單資料，查無訂單應回傳 404 及指定訊息")
    void testGetOrderByOrderNoNotExist() throws Exception {
        String expectedOrderNo = SeedOrderData.orderMaking.orderNo();

        doThrow(new ResourceNotFoundException(MessageEnum.ORDER_NOT_EXIST))
                .when(orderService).getByOrderNo(anyString());

        mockMvc.perform(
                        get("/orders/{orderNo}", expectedOrderNo)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ORDER_NOT_EXIST.getCode()));
    }

    @Test
    @DisplayName("[Unit] OrderController.updateStatusByOrderNo() - 更新訂單狀態成功，應回傳 200")
    void testUpdateOrderStatusByOrderNoSuccess() throws Exception {
        String expectedOrderNo = SeedOrderData.orderMaking.orderNo();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(OrderStatusEnum.PICKED_UP.getCode());

        String jsonBody = objectMapper.writeValueAsString(orderStatusDTO);
        mockMvc.perform(
                        patch("/orders/{orderNo}/status", expectedOrderNo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()));

        verify(orderService).updateStatusByOrderNo(anyString(), any(OrderStatusDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.updateStatusByOrderNo() - 更新訂單狀態，查無訂單應回傳 404 及指定訊息")
    void testUpdateOrderStatusByOrderNoNotExist() throws Exception {
        String expectedOrderNo = SeedOrderData.orderMaking.orderNo();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(OrderStatusEnum.PICKED_UP.getCode());

        doThrow(new ResourceNotFoundException(MessageEnum.ORDER_NOT_EXIST))
                .when(orderService).updateStatusByOrderNo(anyString(), any(OrderStatusDTO.class));

        String jsonBody = objectMapper.writeValueAsString(orderStatusDTO);
        mockMvc.perform(
                        patch("/orders/{orderNo}/status", expectedOrderNo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ORDER_NOT_EXIST.getCode()));

        verify(orderService).updateStatusByOrderNo(anyString(), any(OrderStatusDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.updateStatusByOrderNo() - 更新訂單狀態，訂單狀態不可更新應回傳 400 及指定訊息")
    void testUpdateOrderStatusByOrderNoStatusError() throws Exception {
        String expectedOrderNo = SeedOrderData.orderMaking.orderNo();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(OrderStatusEnum.CANCELLED.getCode());

        doThrow(new BadRequestArgsException(MessageEnum.ORDER_STATUS_TRANSITION_NOT_ALLOWED))
                .when(orderService).updateStatusByOrderNo(anyString(), any(OrderStatusDTO.class));

        String jsonBody = objectMapper.writeValueAsString(orderStatusDTO);
        mockMvc.perform(
                        patch("/orders/{orderNo}/status", expectedOrderNo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.ORDER_STATUS_TRANSITION_NOT_ALLOWED.getCode()));

        verify(orderService).updateStatusByOrderNo(anyString(), any(OrderStatusDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.updateStatusByOrderNo() - 更新訂單狀態，狀態參數超出範圍應回傳 400 及指定訊息")
    void testUpdateOrderStatusByOrderNoInvalidStatus() throws Exception {
        String expectedOrderNo = SeedOrderData.orderMaking.orderNo();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(5);

        String jsonBody = objectMapper.writeValueAsString(orderStatusDTO);
        mockMvc.perform(
                        patch("/orders/{orderNo}/status", expectedOrderNo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.BAD_REQUEST.getCode()));

        verify(orderService, never()).updateStatusByOrderNo(anyString(), any(OrderStatusDTO.class));
    }

    @Test
    @DisplayName("[Unit] OrderController.getOrderCatalog() - 查詢訂單可用產品目錄成功，應回傳 200 及資料")
    void testGetOrderCatalogSuccess() throws Exception {
        OrderCatalogVO mockCatalog = new OrderCatalogVO(
                OrderTestDataFactory.getOrderableProductList(List.of(SeedProductData.MEAT_PRODUCT)),List.of());

        when(orderService.getCatalog()).thenReturn(mockCatalog);

        mockMvc.perform(
                        get("/orders/catalog")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());
        verify(orderService).getCatalog();
    }
}