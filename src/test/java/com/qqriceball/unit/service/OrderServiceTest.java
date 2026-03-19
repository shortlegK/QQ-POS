package com.qqriceball.unit.service;


import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.exception.ResourceNotFoundException;
import com.qqriceball.common.exception.ResourceUnavailableException;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OrderStatusEnum;
import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.mapper.order.OrderItemMapper;
import com.qqriceball.mapper.order.OrderItemOptionMapper;
import com.qqriceball.mapper.order.OrderMapper;
import com.qqriceball.model.dto.order.OrderCreateDTO;
import com.qqriceball.model.dto.order.OrderItemDTO;
import com.qqriceball.model.dto.order.OrderItemOptionDTO;
import com.qqriceball.model.entity.order.Order;
import com.qqriceball.model.entity.order.OrderItem;
import com.qqriceball.model.entity.order.OrderItemOption;
import com.qqriceball.model.vo.OptionVO;
import com.qqriceball.model.vo.ProductVO;
import com.qqriceball.service.OrderService;
import com.qqriceball.testData.option.SeedOptionData;
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

import java.time.LocalDateTime;
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

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.DRINK_OPTIONS, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, optionDTOS);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        Integer expectedTotal = OrderTestDataFactory.calculateTotalPrice(SeedProductData.DRINK_PRODUCT, productQuantity,
                OrderTestDataFactory.DRINK_OPTIONS, optionQuantity);
        int itemNum = orderCreateDTO.getItems().size();
        int optionNum = optionDTOS.size();

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
                () -> assertEquals(optionDTOS.get(0).getOptionId(), optionCaptoredRusults.get(0).getOptionId(), "optionId 應與傳入參數相同"),
                () -> assertEquals(optionDTOS.get(0).getQuantity(), optionCaptoredRusults.get(0).getQuantity(), "quantity 應與傳入參數相同")

        );
    }

    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，product id 不存在，應拋出 ResourceNotFoundException")
    void testCreateProductNotExist() {

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.DRINK_OPTIONS, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, optionDTOS);

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

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.DRINK_OPTIONS, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, optionDTOS);

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

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.DRINK_OPTIONS, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_INACTIVE, productQuantity, optionDTOS);

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

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.DRINK_OPTIONS_INACTIVE, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, optionDTOS);

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

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.DRINK_OPTIONS_WITH_RICE_SIZE, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, optionDTOS);

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

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.DRINK_OPTIONS, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, optionDTOS);

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

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, optionDTOS);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.MEAT_PRODUCT);
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

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITH_ADD_ON, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, optionDTOS);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
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
    @DisplayName("[Unit] OrderService.create() - 建立訂單，單選選項訂購數量超過上限應拋出 BadRequestArgsException")
    void testCreateSingleOptionLimit() {

        Integer optionQuantity = 2;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.DRINK_OPTIONS, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, optionDTOS);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setPickupTime(LocalDateTime.now());
        orderCreateDTO.setItems(List.of(orderItemDTO));

        ProductVO product = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        OptionVO option = OptionTestDataFactory.getOptionVO(SeedOptionData.COLD);

        when(productMapper.getById(any(Integer.class))).thenReturn(product);
        when(optionMapper.getById(any(Integer.class))).thenReturn(option);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> orderService.create(orderCreateDTO));

        assertEquals(MessageEnum.SINGLE_SELECT_OPTION_QUANTITY_EXCEED.getMessage(), exception.getMessage(), "異常訊息應與預期相同");

        verify(productMapper).getById(any(Integer.class));
        verify(optionMapper).getById(any(Integer.class));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(orderItemOptionMapper, never()).insert(any(OrderItemOption.class));
    }

    @Test
    @DisplayName("[Unit] OrderService.create() - 建立訂單，缺少必填選項 BadRequestArgsException")
    void testCreateRequiredOptionMissing() {

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.FOOD_OPTIONS_WITHOUT_SPICE, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, productQuantity, optionDTOS);

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
    @DisplayName("[Unit] OrderService.create() - 建立訂單，飲品設定必填項目以外的選項，應回傳 BadRequestArgsException")
    void testCreateDrinkWithExtraOptions() {

        Integer optionQuantity = 1;
        List<OrderItemOptionDTO> optionDTOS = OrderTestDataFactory.toOptionDTOList(OrderTestDataFactory.DRINK_OPTIONS_WITH_RICE_SIZE, optionQuantity);

        Integer productQuantity = 2;
        OrderItemDTO orderItemDTO = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, productQuantity, optionDTOS);

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

}
