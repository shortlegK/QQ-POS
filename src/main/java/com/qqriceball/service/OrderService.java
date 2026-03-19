package com.qqriceball.service;

import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.exception.ResourceNotFoundException;
import com.qqriceball.common.exception.ResourceUnavailableException;
import com.qqriceball.enumeration.*;
import com.qqriceball.mapper.*;
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
import com.qqriceball.model.vo.order.OrderSummaryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemOptionMapper orderItemOptionMapper;
    private final ProductMapper productMapper;
    private final OptionMapper optionMapper;

    private static final Map<ProductTypeEnum, Set<OptionTypeEnum>> ALLOWED_OPTIONS =
            Map.of(
                    ProductTypeEnum.MEAT,
                    Set.of(OptionTypeEnum.RICE_TYPE, OptionTypeEnum.RICE_SIZE,
                            OptionTypeEnum.SPICE_LEVEL, OptionTypeEnum.ADD_ON
                    ),
                    ProductTypeEnum.VEGAN,
                    Set.of(OptionTypeEnum.RICE_TYPE, OptionTypeEnum.RICE_SIZE,
                            OptionTypeEnum.SPICE_LEVEL, OptionTypeEnum.ADD_ON
                    ),
                    ProductTypeEnum.DRINKS,
                    Set.of(OptionTypeEnum.DRINK_TEMPERATURE
                    )
            );

    private static final Set<OptionTypeEnum> REQUIRED_FOOD_OPTIONS = Set.of(
            OptionTypeEnum.RICE_TYPE,
            OptionTypeEnum.RICE_SIZE,
            OptionTypeEnum.SPICE_LEVEL
    );

    private static final Set<OptionTypeEnum> REQUIRED_DRINK_OPTIONS = Set.of(
            OptionTypeEnum.DRINK_TEMPERATURE
    );

    private static final DateTimeFormatter ORDER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    public OrderService(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                        OrderItemOptionMapper orderItemOptionMapper, ProductMapper productMapper,
                        OptionMapper optionMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderItemOptionMapper = orderItemOptionMapper;
        this.productMapper = productMapper;
        this.optionMapper = optionMapper;
    }


    @Transactional
    public OrderSummaryVO create(OrderCreateDTO orderCreateDTO) {

        // 1. 準備訂單草稿（驗證資料、計算金額、組裝草稿）
        PreparedOrder preparedOrder = prepareOrderDraft(orderCreateDTO.getItems());

        // 2. 確認 pickupTime，建立訂單編號
        LocalDateTime pickupTime = orderCreateDTO.getPickupTime() == null
                ? LocalDateTime.now().plusMinutes(15) : orderCreateDTO.getPickupTime();
        String orderNo = this.generateOrderNo(pickupTime);

        // 3. 建立訂單主表
        Order order = buildOrder(orderNo, preparedOrder.total(),pickupTime);
        orderMapper.insert(order);

        // 4. 建立訂單明細與選項
        insertOrderDetails(order.getId(), preparedOrder.items());

        // 5. 回傳結果摘要
        return buildOrderSummaryVO(order);
    }

    private PreparedOrder prepareOrderDraft(List<OrderItemDTO> itemDTOList) {

        List<PreparedOrderItem> preparedOrderItemList = new ArrayList<>();
        int orderTotal = 0;

        for (OrderItemDTO itemDTO : itemDTOList) {
            // 1. 確認 product 是否存在且為 active 狀態
            ProductVO product = this.getAndValidateProductStatus(itemDTO.getProductId());

            ProductTypeEnum productType = ProductTypeEnum.getByCode(product.getProductType());

            List<OrderItemOption> optionsDraftList = new ArrayList<>();
            Set<OptionTypeEnum> seenSingleOptionTypes = new HashSet<>();
            List<OptionTypeEnum> dtoOptionTypes = new ArrayList<>();
            int optionTotal = 0;

            for (OrderItemOptionDTO itemOptionDTO : itemDTO.getOptions()) {
                // 2. 確認 option 是否存在且為 active 狀態
                OptionVO option = this.getAndValidateOptionStatus(itemOptionDTO.getOptionId());

                OptionTypeEnum optionType = OptionTypeEnum.getByCode(option.getOptionType());
                // 3. 確認 optionType 是否為符合 ProductType 設定規則及單選限制
                this.validateAllowedOptionTypeAndSingleSelect(productType, optionType, seenSingleOptionTypes, itemOptionDTO.getQuantity());
                dtoOptionTypes.add(optionType);

                // 4. 建立 OrderItemOption 資料，計算 option 金額
                OrderItemOption orderItemOption = buildOrderItemOption(option, itemOptionDTO.getQuantity());
                optionsDraftList.add(orderItemOption);
                optionTotal += option.getPrice() * itemOptionDTO.getQuantity();
            }

            // 5. 依據 productType 確認必填 option 是否皆已設定完成
            this.validateRequiredOptionsByProductType(productType, dtoOptionTypes);

            // 6. 計算此商品總金額
            int lineTotal = (product.getPrice() + optionTotal) * itemDTO.getQuantity();

            // 7. 生成 item 訂單草稿
            OrderItem orderItem = buildOrderItem(product, itemDTO.getQuantity(), lineTotal);
            PreparedOrderItem preparedOrderItem = new PreparedOrderItem(orderItem, optionsDraftList);
            preparedOrderItemList.add(preparedOrderItem);
            orderTotal += lineTotal;
        }
        return new PreparedOrder(preparedOrderItemList, orderTotal);
    }

    private ProductVO getAndValidateProductStatus(Integer productId) {
        ProductVO product = productMapper.getById(productId);
        if (product == null) {
            log.error("查無產品資料");
            throw new ResourceNotFoundException(MessageEnum.PRODUCT_NOT_EXIST);
        }
        if (product.getStatus().equals(StatusEnum.INACTIVE.getCode())) {
            log.error("產品為下架狀態,ID: {}", product.getId());
            throw new ResourceUnavailableException(MessageEnum.PRODUCT_UNAVAILABLE);
        }
        return product;
    }

    private OptionVO getAndValidateOptionStatus(Integer optionId) {
        OptionVO option = optionMapper.getById(optionId);
        if (option == null) {
            log.error("查無選項資料");
            throw new ResourceNotFoundException(MessageEnum.OPTION_NOT_EXIST);
        }
        if (option.getStatus().equals(StatusEnum.INACTIVE.getCode())) {
            log.error("選項為下架狀態,ID: {}", option.getId());
            throw new ResourceUnavailableException(MessageEnum.OPTION_UNAVAILABLE);
        }
        return option;
    }

    private void validateAllowedOptionTypeAndSingleSelect(ProductTypeEnum productType, OptionTypeEnum optionType,
                                                          Set<OptionTypeEnum> seenSingleOptions, Integer quantity) {
        // 檢查 ProductType 是否允許使用此 OptionType
        if (!ALLOWED_OPTIONS.get(productType).contains(optionType)) {
            log.error("商品類型: {} 不允許設定選項類型: {}", productType, optionType);
            throw new BadRequestArgsException(MessageEnum.OPTION_TYPE_NOT_ALLOWED);
        }

        // 檢查是否有設定重複單選的 Option
        if (optionType.isSingleSelect() && !seenSingleOptions.add(optionType)) {
            log.error("單選項目重複設定: {}", optionType);
            throw new BadRequestArgsException(MessageEnum.DUPLICATE_OPTION);
        }

        // 檢查單選選項訂購數量是否超過限制
        if(optionType.isSingleSelect() && quantity > optionType.getLimit()){
            log.error("單選項目數量錯誤: {} 數量: {}", optionType, quantity);
            throw new BadRequestArgsException(MessageEnum.SINGLE_SELECT_OPTION_QUANTITY_EXCEED);
        }

    }

    private void validateRequiredOptionsByProductType(ProductTypeEnum productType, List<OptionTypeEnum> dtoOptionTypes) {
        Set<OptionTypeEnum> requiredList = switch (productType) {
            case DRINKS -> REQUIRED_DRINK_OPTIONS;
            case MEAT, VEGAN -> REQUIRED_FOOD_OPTIONS;
        };

        // 檢查已選選項是否包含所有必填項目
        requiredList.forEach(required -> {
            if (!dtoOptionTypes.contains(required)) {
                log.error("缺少必填選項類型:{}", required);
                throw new BadRequestArgsException(MessageEnum.REQUIRED_OPTION_MISSING);
            }
        });

        // 飲品的必選選項皆為單選，且無法設定其他選項，已設 optionType 數量應與必選選項清單相同
        if (productType.equals(ProductTypeEnum.DRINKS) && dtoOptionTypes.size() != requiredList.size()) {
            log.error("商品類型: {} 選項設定錯誤: {}", productType, dtoOptionTypes);
            throw new BadRequestArgsException(MessageEnum.OPTION_TYPE_NOT_ALLOWED);
        }

    }

    private String generateOrderNo(LocalDateTime pickupTime) {
        int sequence = 1;
        String start = pickupTime.format(ORDER_DATE_FORMAT);
        String end = pickupTime.plusDays(1).format(ORDER_DATE_FORMAT);
        String maxOrderNo = orderMapper.getMaxOrderNoByPickupTime(start, end);

        if (maxOrderNo != null) {
            String maxOrderSeq = maxOrderNo.substring(8);
            sequence = Integer.parseInt(maxOrderSeq) + 1;
        }
        return start + String.format("%04d", sequence);
    }

    private OrderItemOption buildOrderItemOption(OptionVO option, Integer quantity) {
        OrderItemOption orderItemOption = new OrderItemOption();
        orderItemOption.setOptionId(option.getId());
        orderItemOption.setOptionType(option.getOptionType());
        orderItemOption.setOptionTitle(option.getTitle());
        orderItemOption.setOptionPrice(option.getPrice());
        orderItemOption.setQuantity(quantity);
        return orderItemOption;
    }

    private OrderItem buildOrderItem(ProductVO product, Integer quantity, Integer lineTotal) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(product.getId());
        orderItem.setProductType(product.getProductType());
        orderItem.setProductTitle(product.getTitle());
        orderItem.setProductPrice(product.getPrice());
        orderItem.setQuantity(quantity);
        orderItem.setLineTotal(lineTotal);
        return orderItem;
    }

    private Order buildOrder(String orderNo, Integer total,LocalDateTime pickupTime) {
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setPickupTime(pickupTime);
        order.setTotal(total);
        order.setStatus(OrderStatusEnum.MAKING.getCode());
        return order;
    }


    private void insertOrderDetails(Integer orderId, List<PreparedOrderItem> preparedOrderItemList) {
        for (PreparedOrderItem preparedOrderItem : preparedOrderItemList) {
            OrderItem orderItem = preparedOrderItem.item();
            orderItem.setOrderId(orderId);
            orderItemMapper.insert(orderItem);
            insertOrderItemOptions(orderItem.getId(), preparedOrderItem.options());
        }
    }

    private void insertOrderItemOptions(Integer orderItemId, List<OrderItemOption> orderItemOptionList) {
        for (OrderItemOption orderItemOption : orderItemOptionList) {
            orderItemOption.setOrderItemId(orderItemId);
            orderItemOptionMapper.insert(orderItemOption);
        }
    }

    private OrderSummaryVO buildOrderSummaryVO(Order order) {
        OrderSummaryVO orderSummaryVO = new OrderSummaryVO();
        BeanUtils.copyProperties(order, orderSummaryVO);
        return orderSummaryVO;
    }

    private record PreparedOrder(List<PreparedOrderItem> items, Integer total) {
    }

    private record PreparedOrderItem(OrderItem item, List<OrderItemOption> options) {
    }

}
