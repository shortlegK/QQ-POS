package com.qqriceball.unit.service;


import com.github.pagehelper.Page;
import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.exception.ResourceNotFoundException;
import com.qqriceball.common.exception.ResourceUnavailableException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OrderStatusEnum;
import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.mapper.order.OrderItemMapper;
import com.qqriceball.mapper.order.OrderItemOptionMapper;
import com.qqriceball.mapper.order.OrderMapper;
import com.qqriceball.model.dto.order.*;
import com.qqriceball.model.entity.order.Order;
import com.qqriceball.model.entity.order.OrderItem;
import com.qqriceball.model.entity.order.OrderItemOption;
import com.qqriceball.model.vo.option.OptionVO;
import com.qqriceball.model.vo.product.ProductVO;
import com.qqriceball.model.vo.order.OrderDetailVO;
import com.qqriceball.model.vo.order.OrderItemOptionVO;
import com.qqriceball.model.vo.order.OrderItemVO;
import com.qqriceball.model.vo.order.OrderSummaryVO;
import com.qqriceball.model.vo.order.catalog.OrderCatalogVO;
import com.qqriceball.model.vo.order.catalog.OrderableOptionVO;
import com.qqriceball.model.vo.order.catalog.OrderableProductVO;
import com.qqriceball.service.OrderService;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.testData.order.SeedOrderData;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.utils.option.OptionTestDataFactory;
import com.qqriceball.utils.order.OrderTestDataFactory;
import com.qqriceball.utils.product.ProductTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private OrderItemOptionMapper orderItemOptionMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private OptionMapper optionMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，應呼叫 orderMapper.insert 傳入參數")
    void testCreateSuccess() {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.MINUTES));
        orderCreateDTO.setItems(List.of(orderItemDTO));

        Integer expectedTotal = OrderTestDataFactory.calculateTotalPrice(SeedProductData.DRINK_PRODUCT, productQuantity,
                OrderTestDataFactory.DRINK_OPTIONS);
        int itemNum = orderCreateDTO.getItems().size();
        int optionNum = optionIdsList.size();

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        OptionVO option = OptionTestDataFactory.getOptionVO(SeedOptionData.COLD);

        when(productMapper.getById(any(Integer.class))).thenReturn(product);
        when(optionMapper.getById(any(Integer.class))).thenReturn(option);

        orderService.create(orderCreateDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).insert(orderCaptor.capture());
        Order orderCaptoredRusult = orderCaptor.getValue();

        ArgumentCaptor<OrderItem> orderItemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemMapper, times(itemNum)).insert(orderItemCaptor.capture());
        List<OrderItem> itemCaptoredRusults = orderItemCaptor.getAllValues();

        ArgumentCaptor<OrderItemOption> orderItemOptionCaptor = ArgumentCaptor.forClass(OrderItemOption.class);
        verify(orderItemOptionMapper, times(optionNum)).insert(orderItemOptionCaptor.capture());
        List<OrderItemOption> optionCaptoredRusults = orderItemOptionCaptor.getAllValues();

        assertAll(
                () -> assertEquals(orderCreateDTO.getPickupTime(), orderCaptoredRusult.getPickupTime(), "pickupTime 應與傳入參數相同"),
                () -> assertEquals(OrderStatusEnum.MAKING.getCode(),orderCaptoredRusult.getStatus(), "建立訂單，狀態應為製作中"),
                () -> assertEquals(expectedTotal, orderCaptoredRusult.getTotal(), "totalPrice 應為產品價格加上選項價格乘以數量的總和"),
                () -> assertEquals(optionNum, optionCaptoredRusults.size(), " orderItemOption 新增次數應與傳入 DTO 數量相同"),
                () -> assertEquals(itemNum, itemCaptoredRusults.size(), "orderItem 新增次數應與傳入 DTO 數量相同"),
                () -> assertEquals(orderItemDTO.getProductId(), itemCaptoredRusults.get(0).getProductId(), "productId 應與傳入參數相同"),
                () -> assertEquals(orderItemDTO.getQuantity(), itemCaptoredRusults.get(0).getQuantity(), "quantity 應與傳入參數相同"),
                () -> assertEquals(optionIdsList.get(0), optionCaptoredRusults.get(0).getOptionId(), "optionId 應與傳入參數相同")
        );
    }

    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，重複設定加料選項，totalPrice 應正確計算，重複的加料選項應計算一次")
    void testCreateDuplicateAddOnOption() {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);
        optionIdsList.add(SeedOptionData.EGG.id());
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, 1, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO mockProduct = ProductTestDataFactory.getProductVO(SeedProductData.MEAT_PRODUCT);

        Integer expectedTotal = OrderTestDataFactory.calculateTotalPrice(SeedProductData.MEAT_PRODUCT, 1,
                OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM) + SeedOptionData.EGG.price();

        when(productMapper.getById(any(Integer.class))).thenReturn(mockProduct);

        when(optionMapper.getById(any(Integer.class))).thenReturn(OptionTestDataFactory.getOptionVO(SeedOptionData.LARGE_SIZE))
                .thenReturn(OptionTestDataFactory.getOptionVO(SeedOptionData.MILD_SPICY))
                .thenReturn(OptionTestDataFactory.getOptionVO(SeedOptionData.PURPLE_RICE))
                .thenReturn(OptionTestDataFactory.getOptionVO(SeedOptionData.EGG))
                .thenReturn(OptionTestDataFactory.getOptionVO(SeedOptionData.EGG))
                .thenReturn(OptionTestDataFactory.getOptionVO(SeedOptionData.NO_ONION));

        OrderSummaryVO summaryVO = orderService.create(orderCreateDTO);
        assertEquals(expectedTotal, summaryVO.getTotal(), "totalPrice 應為產品價格加上選項價格乘以數量的總和，重複的加料選項應計算一次");
    }

    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，product id 不存在，應拋出 ResourceNotFoundException")
    void testCreateProductNotExist() {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        when(productMapper.getById(any(Integer.class))).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.create(orderCreateDTO));

        assertEquals(MessageEnum.PRODUCT_NOT_EXIST.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

        verify(productMapper).getById(any(Integer.class));
        verify(optionMapper, never()).getById(any(Integer.class));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(orderItemOptionMapper, never()).insert(any(OrderItemOption.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，option id 不存在，應拋出 ResourceNotFoundException")
    void testCreateOptionNotExist() {

        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        when(productMapper.getById(any(Integer.class))).thenReturn(product);
        when(optionMapper.getById(any(Integer.class))).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.create(orderCreateDTO));

        assertEquals(MessageEnum.OPTION_NOT_EXIST.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

        verify(productMapper).getById(any(Integer.class));
        verify(optionMapper).getById(any(Integer.class));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(orderItemOptionMapper, never()).insert(any(OrderItemOption.class));
    }


    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，productStatus = Inactive，應拋出 ResourceUnavailableException")
    void testCreateProductInactive() {

        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_INACTIVE, 2, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_INACTIVE);

        when(productMapper.getById(any(Integer.class))).thenReturn(product);

        ResourceUnavailableException exception = assertThrows(ResourceUnavailableException.class,
                () -> orderService.create(orderCreateDTO));

        assertEquals(MessageEnum.PRODUCT_UNAVAILABLE.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

        verify(productMapper).getById(any(Integer.class));
        verify(optionMapper, never()).getById(any(Integer.class));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(orderItemOptionMapper, never()).insert(any(OrderItemOption.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，optionStatus = Inactive，應拋出 ResourceUnavailableException")
    void testCreateOptionInactive() {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS_INACTIVE);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        OptionVO option = OptionTestDataFactory.getOptionVO(SeedOptionData.TEMP_INACTIVE);

        when(productMapper.getById(any(Integer.class))).thenReturn(product);
        when(optionMapper.getById(any(Integer.class))).thenReturn(option);

        ResourceUnavailableException exception = assertThrows(ResourceUnavailableException.class,
                () -> orderService.create(orderCreateDTO));

        assertEquals(MessageEnum.OPTION_UNAVAILABLE.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

        verify(productMapper).getById(any(Integer.class));
        verify(optionMapper).getById(any(Integer.class));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(orderItemOptionMapper, never()).insert(any(OrderItemOption.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，飲品含有不允許的 option，應拋出 BadRequestArgsException")
    void testCreateDrinkOptionNotAllow() {

        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS_WITH_RICE_SIZE);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        OptionVO option = OptionTestDataFactory.getOptionVO(SeedOptionData.WHITE_RICE);

        when(productMapper.getById(any(Integer.class))).thenReturn(product);
        when(optionMapper.getById(any(Integer.class))).thenReturn(option);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> orderService.create(orderCreateDTO));

        assertEquals(MessageEnum.OPTION_TYPE_NOT_ALLOWED.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

        verify(productMapper).getById(any(Integer.class));
        verify(optionMapper).getById(any(Integer.class));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(orderItemOptionMapper, never()).insert(any(OrderItemOption.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，食物含有不允許的 option，應拋出 BadRequestArgsException")
    void testCreateFoodOptionNotAllow() {

        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, 2, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.MEAT_PRODUCT);
        OptionVO option = OptionTestDataFactory.getOptionVO(SeedOptionData.COLD);

        when(productMapper.getById(any(Integer.class))).thenReturn(product);
        when(optionMapper.getById(any(Integer.class))).thenReturn(option);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> orderService.create(orderCreateDTO));

        assertEquals(MessageEnum.OPTION_TYPE_NOT_ALLOWED.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

        verify(productMapper).getById(any(Integer.class));
        verify(optionMapper).getById(any(Integer.class));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(orderItemOptionMapper, never()).insert(any(OrderItemOption.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，食物設定重複的單選選項應拋出 BadRequestArgsException")
    void testCreateFoodSingleOptionDuplicate() {

        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, 2, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.MEAT_PRODUCT);
        // 欲新增訂單的設有多項Option，查詢 Option 回傳的資料為同一個類型，模擬使用者傳入重複的單選選項
        OptionVO option = OptionTestDataFactory.getOptionVO(SeedOptionData.LARGE_SIZE);

        when(productMapper.getById(any(Integer.class))).thenReturn(product);
        when(optionMapper.getById(any(Integer.class))).thenReturn(option);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> orderService.create(orderCreateDTO));

        assertEquals(MessageEnum.DUPLICATE_OPTION.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

        verify(productMapper).getById(any(Integer.class));
        verify(optionMapper,times(2)).getById(any(Integer.class));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(orderItemOptionMapper, never()).insert(any(OrderItemOption.class));
    }


    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，飲品設定重複的單選選項應拋出 BadRequestArgsException")
    void testCreateDrinkSingleOptionDuplicate() {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        // 欲新增訂單的設有多項Option，查詢 Option 回傳的資料為同一個飲品選項類型，模擬使用者傳入重複的單選選項
        OptionVO option = OptionTestDataFactory.getOptionVO(SeedOptionData.COLD);

        when(productMapper.getById(any(Integer.class))).thenReturn(product);
        when(optionMapper.getById(any(Integer.class))).thenReturn(option);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> orderService.create(orderCreateDTO));

        assertEquals(MessageEnum.DUPLICATE_OPTION.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

        verify(productMapper).getById(any(Integer.class));
        verify(optionMapper,times(2)).getById(any(Integer.class));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(orderItemOptionMapper, never()).insert(any(OrderItemOption.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，缺少必填選項，應拋出 BadRequestArgsException")
    void testCreateRequiredOptionMissing() {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.FOOD_OPTIONS_WITHOUT_SPICE);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, 2, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.MEAT_PRODUCT);
        OptionVO optionRiceType = OptionTestDataFactory.getOptionVO(SeedOptionData.WHITE_RICE);
        OptionVO optionRiceSize = OptionTestDataFactory.getOptionVO(SeedOptionData.NORMAL_SIZE);

        when(productMapper.getById(any(Integer.class))).thenReturn(product);
        when(optionMapper.getById(SeedOptionData.WHITE_RICE.id())).thenReturn(optionRiceType);
        when(optionMapper.getById(SeedOptionData.NORMAL_SIZE.id())).thenReturn(optionRiceSize);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> orderService.create(orderCreateDTO));

        assertEquals(MessageEnum.REQUIRED_OPTION_MISSING.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

        verify(productMapper).getById(any(Integer.class));
        verify(optionMapper,times(2)).getById(any(Integer.class));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(orderItemOptionMapper, never()).insert(any(OrderItemOption.class));
    }


    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，飲品設定必填項目以外的選項，應拋出 BadRequestArgsException")
    void testCreateDrinkWithExtraOptions() {

        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS_WITH_RICE_SIZE);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, optionIdsList);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        OptionVO optionTemp = OptionTestDataFactory.getOptionVO(SeedOptionData.COLD);
        OptionVO optionRiceSize = OptionTestDataFactory.getOptionVO(SeedOptionData.NORMAL_SIZE);

        when(productMapper.getById(any(Integer.class))).thenReturn(product);
        when(optionMapper.getById(SeedOptionData.COLD.id())).thenReturn(optionTemp);
        when(optionMapper.getById(SeedOptionData.NORMAL_SIZE.id())).thenReturn(optionRiceSize);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> orderService.create(orderCreateDTO));

        assertEquals(MessageEnum.OPTION_TYPE_NOT_ALLOWED.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

        verify(productMapper).getById(any(Integer.class));
        verify(optionMapper,times(2)).getById(any(Integer.class));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(orderItemOptionMapper, never()).insert(any(OrderItemOption.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.pageQuery() - 查詢訂單列表，應呼叫 Order 相關 Mapper 傳入參數")
    void testPageQuerySuccess() {
        Integer page = 1;
        Integer pageSize = 2;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(2);

        OrderPageQueryDTO orderPageQueryDTO = OrderTestDataFactory.getOrderPageQueryDTO(page, pageSize, null, null, startDate, endDate);

        Page<OrderDetailVO> mockPage = new Page<>(page, pageSize);
        mockPage.add(OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderMaking,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM));

        when(orderMapper.pageQuery(any(OrderPageQueryDTO.class))).thenReturn(mockPage);

        PageResult result = orderService.pageQuery(orderPageQueryDTO);

        assertAll(
                () -> assertEquals(page, result.getPage(), "page 應為傳入的 page"),
                () -> assertEquals(pageSize, result.getPageSize(), "pageSize 應為傳入的 pageSize"),
                () -> assertEquals(mockPage.getResult(), result.getRecords(), "list 應為 mock page 的 result")
        );
    }

    @Test
    @DisplayName("[Unit] OrderService.pageQuery() - 查詢指定編號，未設定起迄日期，應呼叫 orderMapper.pageQuery 傳入指定 orderNo")
    void testPageQueryWithOrderNo() {
        String orderNo = SeedOrderData.orderMaking.orderNo();

        OrderPageQueryDTO orderPageQueryDTO = OrderTestDataFactory.getOrderPageQueryDTO(1, 10, orderNo, null, null, null);

        Page<OrderDetailVO> mockPage = new Page<>(1, 10);
        mockPage.add(OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderMaking,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM));

        when(orderMapper.pageQuery(any(OrderPageQueryDTO.class))).thenReturn(mockPage);

        PageResult result = orderService.pageQuery(orderPageQueryDTO);

        ArgumentCaptor<OrderPageQueryDTO> queryCaptor = ArgumentCaptor.forClass(OrderPageQueryDTO.class);
        verify(orderMapper).pageQuery(queryCaptor.capture());
        OrderPageQueryDTO capturedQuery = queryCaptor.getValue();

        assertAll(
                () -> assertEquals(orderNo, capturedQuery.getOrderNo(), "應依據傳入的 orderNo 查詢"),
                () -> assertEquals(mockPage.getResult(), result.getRecords(), "list 應為 mock page 的 result")
        );
    }

    @Test
    @DisplayName("[Unit] OrderService.pageQuery() - 查詢未設定 orderNo、起迄日期，應呼叫 orderMapper.pageQuery 傳入當天日期")
    void testPageQueryWithoutOrderNoAndDate() {
        LocalDate today = LocalDate.now();

        OrderPageQueryDTO orderPageQueryDTO = OrderTestDataFactory.getOrderPageQueryDTO(1, 10, null, null, null, null);

        Page<OrderDetailVO> mockPage = new Page<>(1, 10);
        mockPage.add(OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderMaking,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM));

        when(orderMapper.pageQuery(any(OrderPageQueryDTO.class))).thenReturn(mockPage);

        PageResult result = orderService.pageQuery(orderPageQueryDTO);

        ArgumentCaptor<OrderPageQueryDTO> queryCaptor = ArgumentCaptor.forClass(OrderPageQueryDTO.class);
        verify(orderMapper).pageQuery(queryCaptor.capture());
        OrderPageQueryDTO capturedQuery = queryCaptor.getValue();

        assertAll(
                () -> assertEquals(today, capturedQuery.getStartDate(), "當未設定起迄日期，startDate 應為當天日期"),
                () -> assertEquals(today, capturedQuery.getEndDate(), "當未設定起迄日期，endDate 應為當天日期"),
                () -> assertEquals(mockPage.getResult(), result.getRecords(), "list 應為 mock page 的 result")
        );
    }


    @Test
    @DisplayName("[Unit] OrderService.updateByOrderNo() - 更新訂單資料，應呼叫 orderMapper.updateByOrderNo 傳入參數")
    void testUpdateByOrderNoSuccess() {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, optionIdsList);

        OrderEditDTO orderEditDTO = new OrderEditDTO();
        orderEditDTO.setPickupTime(LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.MINUTES));
        orderEditDTO.setItems(List.of(orderItemDTO));
        orderEditDTO.setOrderNo(SeedOrderData.orderMaking.orderNo());

        Integer expectedTotal = OrderTestDataFactory.calculateTotalPrice(SeedProductData.DRINK_PRODUCT, productQuantity,
                OrderTestDataFactory.DRINK_OPTIONS);

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        OptionVO option = OptionTestDataFactory.getOptionVO(SeedOptionData.COLD);

        OrderDetailVO mockExistingOrder = OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderMaking,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);

        Integer mockOrderId = mockExistingOrder.getId();
        Integer mockItem0Id = mockExistingOrder.getItems().get(0).getId();
        int mockItemNum = mockExistingOrder.getItems().size();

        when(orderMapper.getByOrderNo(any(String.class))).thenReturn(mockExistingOrder);
        when(productMapper.getById(any(Integer.class))).thenReturn(product);
        when(optionMapper.getById(any(Integer.class))).thenReturn(option);
        when(orderItemMapper.getItemsByOrderId(any(Integer.class))).thenReturn(mockExistingOrder.getItems());

        for (OrderItemVO item : mockExistingOrder.getItems()) {
            when(orderItemOptionMapper.getOptionsByItemId(item.getId())).thenReturn(item.getOptions());
        }

        OrderSummaryVO updateResult = orderService.updateByOrderNo(orderEditDTO);

        ArgumentCaptor<Integer> deleteItemCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(orderItemMapper).deleteItemsByOrderId(deleteItemCaptor.capture());
        Integer deleteItemCaptoredRusult = deleteItemCaptor.getValue();

        ArgumentCaptor<Integer> deleteOptionCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(orderItemOptionMapper, times(mockItemNum)).deleteOptionsByItemId(deleteOptionCaptor.capture());
        List<Integer> deleteOptionCaptoredRusult = deleteOptionCaptor.getAllValues();

        assertAll(
                () -> assertEquals(mockOrderId,deleteItemCaptoredRusult , "應依據 OrderId 刪除原有的 orderItem"),
                () -> assertEquals(mockItem0Id, deleteOptionCaptoredRusult.get(0), "應依據 ItemId 刪除原有的 orderItemOption"),
                () -> assertEquals(orderEditDTO.getPickupTime(), updateResult.getPickupTime(), "pickupTime 應與傳入參數相同"),
                () -> assertEquals(OrderStatusEnum.MAKING.getCode(),updateResult.getStatus(), "更新訂單，狀態應為製作中"),
                () -> assertEquals(expectedTotal, updateResult.getTotal(), "totalPrice 應為產品價格加上選項價格乘以數量的總和"),
                () -> assertEquals(orderEditDTO.getOrderNo(), updateResult.getOrderNo(), "orderNo 應與傳入參數相同")
        );
    }

    @Test
    @DisplayName("[Unit] OrderService.updateByOrderNo() - 更新訂單，查無訂單資料，應拋出 ResourceNotFoundException")
    void testUpdateByOrderNoStatusNotMaking() {
        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdsList);

        OrderEditDTO orderEditDTO = new OrderEditDTO();
        orderEditDTO.setItems(List.of(orderItemDTO));
        orderEditDTO.setOrderNo(SeedOrderData.orderMaking.orderNo());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateByOrderNo(orderEditDTO));

        assertEquals(MessageEnum.ORDER_NOT_EXIST.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

    }


    @Test
    @DisplayName("[Unit] OrderService.updateByOrderNo() - 更新訂單，訂單狀態非製作中，應拋出 ResourceUnavailableException")
    void testUpdateByOrderNoOrderNotMaking() {

        List<Integer> optionIdsList = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, 2, optionIdsList);

        OrderEditDTO orderEditDTO = new OrderEditDTO();
        orderEditDTO.setItems(List.of(orderItemDTO));
        orderEditDTO.setOrderNo(SeedOrderData.orderMaking.orderNo());

        OrderDetailVO mockExistingOrder = OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderReady,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);

        when(orderMapper.getByOrderNo(any(String.class))).thenReturn(mockExistingOrder);

        ResourceUnavailableException exception = assertThrows(ResourceUnavailableException.class,
                () -> orderService.updateByOrderNo(orderEditDTO));

        assertEquals(MessageEnum.ORDER_NOT_EDITABLE.getMessage(), exception.getMessage(), "異常訊息應與預期相同");
    }

    @Test
    @DisplayName("[Unit] OrderService.getByOrderNo() - 查詢訂單資料，應呼叫 orderMapper.getByOrderNo、 orderItemMapper.getItemsByOrderId、orderItemOptionMapper.getOptionsByItemId 傳入參數")
    void testGetByOrderNoSuccess() {
        String expectedOrderNo = SeedOrderData.orderMaking.orderNo();

        OrderDetailVO mockOrderDetail = OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderMaking,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);
        List<OrderItemVO> mockItemList = mockOrderDetail.getItems();
        List<OrderItemOptionVO> mockOptionList = mockItemList.get(0).getOptions();

        Integer expectedOrderId = mockOrderDetail.getId();
        Integer expectedItemId = mockItemList.get(0).getId();

        when(orderMapper.getByOrderNo(any(String.class))).thenReturn(mockOrderDetail);
        when(orderItemMapper.getItemsByOrderId(any(Integer.class))).thenReturn(mockItemList);
        when(orderItemOptionMapper.getOptionsByItemId(any(Integer.class))).thenReturn(mockOptionList);

        OrderDetailVO result = orderService.getByOrderNo(expectedOrderNo);

        ArgumentCaptor<String> getOrderCaptor = ArgumentCaptor.forClass(String.class);
        verify(orderMapper).getByOrderNo(getOrderCaptor.capture());
        String getOrderCaptoredRusult = getOrderCaptor.getValue();

        ArgumentCaptor<Integer> getItemCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(orderItemMapper).getItemsByOrderId(getItemCaptor.capture());
        Integer getItemCaptoredRusult = getItemCaptor.getValue();

        ArgumentCaptor<Integer> getOptionCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(orderItemOptionMapper).getOptionsByItemId(getOptionCaptor.capture());
        Integer getOptionCaptoredRusult = getOptionCaptor.getValue();

        assertAll(
                () -> assertEquals(mockOrderDetail, result, "回傳結果應與 mockOrderDetail 相同"),
                () -> assertEquals(expectedOrderNo, getOrderCaptoredRusult, "應依據 orderNo 查詢訂單"),
                () -> assertEquals(expectedOrderId, getItemCaptoredRusult, "應依據 OrderId 查詢訂單項目"),
                () -> assertEquals(expectedItemId, getOptionCaptoredRusult, "應依據 ItemId 查詢訂單選項")
        );
    }


    @Test
    @DisplayName("[Unit] OrderService.getByOrderNo() - 查詢訂單資料，查無訂單編號，應拋出 ResourceNotFoundException")
    void testGetByOrderNoNotExist() {
        String orderNo = SeedOrderData.orderMaking.orderNo();

        when(orderMapper.getByOrderNo(any(String.class))).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getByOrderNo(orderNo));

        assertEquals(MessageEnum.ORDER_NOT_EXIST.getMessage(), exception.getMessage(), "異常訊息應與預期相同");
        verify(orderMapper).getByOrderNo(any(String.class));
        verify(orderItemMapper, never()).getItemsByOrderId(any(Integer.class));
        verify(orderItemOptionMapper, never()).getOptionsByItemId(any(Integer.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.updateStatusByOrderNo() - 更改訂單狀態，應呼叫 orderMapper.updateByOrderNo 傳入參數")
    void testUpdateStatusByOrderNoSuccess() {
        String orderNo = SeedOrderData.orderMaking.orderNo();
        Integer newStatus = OrderStatusEnum.READY.getCode();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(newStatus);

        OrderDetailVO mockExistingOrder = OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderMaking,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);

        when(orderMapper.getByOrderNo(any(String.class))).thenReturn(mockExistingOrder);

        orderService.updateStatusByOrderNo(orderNo, orderStatusDTO);

        ArgumentCaptor<Order> updateOrderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateByOrderNo(updateOrderCaptor.capture());

        Order capturedOrder = updateOrderCaptor.getValue();

        assertAll(
                () -> assertEquals(orderNo, capturedOrder.getOrderNo(), "應依據 orderNo 更新訂單狀態"),
                () -> assertEquals(newStatus, capturedOrder.getStatus(), "更新的狀態應與傳入參數相同")
        );
    }

    @Test
    @DisplayName("[Unit] OrderService.updateStatusByOrderNo() - 更改訂單狀態，查無訂單編號，應拋出 ResourceNotFoundException")
    void testUpdateStatusByOrderNoNotExist() {
        String orderNo = SeedOrderData.orderMaking.orderNo();
        Integer newStatus = OrderStatusEnum.READY.getCode();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(newStatus);

        when(orderMapper.getByOrderNo(any(String.class))).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateStatusByOrderNo(orderNo, orderStatusDTO));

        assertEquals(MessageEnum.ORDER_NOT_EXIST.getMessage(), exception.getMessage(), "異常訊息應與預期相同");
        verify(orderMapper).getByOrderNo(any(String.class));
        verify(orderMapper, never()).updateByOrderNo(any(Order.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.updateStatusByOrderNo() - 更改訂單狀態，Ready 無法轉換為 Making，應拋出 BadRequestArgsException")
    void testUpdateStatusByOrderNoReadyCanNotTransitionToMaking() {
        String orderNo = SeedOrderData.orderReady.orderNo();
        Integer newStatus = OrderStatusEnum.MAKING.getCode();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(newStatus);

        OrderDetailVO mockExistingOrder = OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderMaking,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);

        when(orderMapper.getByOrderNo(any(String.class))).thenReturn(mockExistingOrder);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> orderService.updateStatusByOrderNo(orderNo, orderStatusDTO));

        assertEquals(MessageEnum.ORDER_STATUS_TRANSITION_NOT_ALLOWED.getMessage(), exception.getMessage(), "異常訊息應與預期相同");
        verify(orderMapper).getByOrderNo(any(String.class));
        verify(orderMapper, never()).updateByOrderNo(any(Order.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.updateStatusByOrderNo() - 更改訂單狀態，Ready 無法轉換為 Cancel，應拋出 BadRequestArgsException")
    void testUpdateStatusByOrderNoReadyCanNotTransitionToCancel() {
        String orderNo = SeedOrderData.orderReady.orderNo();
        Integer newStatus = OrderStatusEnum.CANCELLED.getCode();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(newStatus);

        OrderDetailVO mockExistingOrder = OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderReady,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);

        when(orderMapper.getByOrderNo(any(String.class))).thenReturn(mockExistingOrder);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> orderService.updateStatusByOrderNo(orderNo, orderStatusDTO));

        assertEquals(MessageEnum.ORDER_STATUS_TRANSITION_NOT_ALLOWED.getMessage(), exception.getMessage(), "異常訊息應與預期相同");
        verify(orderMapper).getByOrderNo(any(String.class));
        verify(orderMapper, never()).updateByOrderNo(any(Order.class));
    }


    @Test
    @DisplayName("[Unit] OrderService.updateStatusByOrderNo() - 更改訂單狀態， PickedUp 無法轉換為其他狀態，應拋出 BadRequestArgsException")
    void testUpdateStatusByOrderNoPickedUpCanNotTransitionToOtherStatus(){
        String orderNo = SeedOrderData.orderPickedUp.orderNo();
        Integer newStatus = OrderStatusEnum.CANCELLED.getCode();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(newStatus);

        OrderDetailVO mockExistingOrder = OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderPickedUp,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);

        when(orderMapper.getByOrderNo(any(String.class))).thenReturn(mockExistingOrder);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> orderService.updateStatusByOrderNo(orderNo, orderStatusDTO));

        assertEquals(MessageEnum.ORDER_STATUS_TRANSITION_NOT_ALLOWED.getMessage(), exception.getMessage(), "異常訊息應與預期相同");
        verify(orderMapper).getByOrderNo(any(String.class));
        verify(orderMapper, never()).updateByOrderNo(any(Order.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.updateStatusByOrderNo() - 更改訂單狀態，Cancel 無法轉換為其他狀態，應拋出 BadRequestArgsException")
    void testUpdateStatusByOrderNoCancelCanNotTransitionToOtherStatus() {
        String orderNo = SeedOrderData.orderCancel.orderNo();
        Integer newStatus = OrderStatusEnum.READY.getCode();
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(newStatus);

        OrderDetailVO mockExistingOrder = OrderTestDataFactory.getOrderDetailVO(SeedOrderData.orderCancel,
                SeedProductData.MEAT_PRODUCT, OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);

        when(orderMapper.getByOrderNo(any(String.class))).thenReturn(mockExistingOrder);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> orderService.updateStatusByOrderNo(orderNo, orderStatusDTO));

        assertEquals(MessageEnum.ORDER_STATUS_TRANSITION_NOT_ALLOWED.getMessage(), exception.getMessage(), "異常訊息應與預期相同");
        verify(orderMapper).getByOrderNo(any(String.class));
        verify(orderMapper, never()).updateByOrderNo(any(Order.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.getCatalog() - 查詢產品目錄，應回傳已上架產品列表及可用選項資料")
    void testGetCatalogSuccess() {

        List<OrderableProductVO> mockProductList = OrderTestDataFactory.getOrderableProductList(
                List.of(SeedProductData.MEAT_PRODUCT, SeedProductData.VEG_PRODUCT));

        List<OrderableOptionVO> mockOptionList = OrderTestDataFactory.getOrderableOptionList(
                List.of(SeedOptionData.NORMAL_SIZE,SeedOptionData.LARGE_SIZE));

        when(productMapper.getActiveProducts()).thenReturn(mockProductList);
        when(optionMapper.getActiveOptionsByType(anyInt())).thenReturn(mockOptionList);

        OrderCatalogVO result = orderService.getCatalog();

        verify(productMapper).getActiveProducts();
        verify(optionMapper, times(11)).getActiveOptionsByType(anyInt()); // OrderService ALLOWED_OPTIONS List 的 Enum 總數

        assertAll(
                () -> assertEquals(mockProductList, result.getProducts(), "回傳的產品列表應與 mockProductList 相同"),
                () -> assertEquals(ProductTypeEnum.values().length, result.getOptionConfigs().size(), "回傳的選項列表應與 mockOptionList 相同"),
                () -> assertEquals(mockOptionList, result.getOptionConfigs().get(0).getOptionGroups().get(0).getOptions(), "回傳的選項列表應與 mockOptionList 相同")
        );
    }
}
