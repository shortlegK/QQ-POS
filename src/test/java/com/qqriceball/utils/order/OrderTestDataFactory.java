package com.qqriceball.utils.order;

import com.qqriceball.model.dto.order.OrderItemDTO;
import com.qqriceball.model.dto.order.OrderPageQueryDTO;
import com.qqriceball.model.vo.order.OrderDetailVO;
import com.qqriceball.model.vo.order.OrderItemOptionVO;
import com.qqriceball.model.vo.order.OrderItemVO;
import com.qqriceball.model.vo.order.catalog.OrderableOptionVO;
import com.qqriceball.model.vo.order.catalog.OrderableProductVO;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.testData.option.TestOption;
import com.qqriceball.testData.order.TestOrder;
import com.qqriceball.testData.product.TestProduct;
import com.qqriceball.utils.TestDataGenerator;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderTestDataFactory {

    public static final List<TestOption> FOOD_OPTIONS_WITH_OPTIONAL_ITEM = List.of(
            SeedOptionData.LARGE_SIZE,
            SeedOptionData.MEDIUM_SPICY,
            SeedOptionData.PURPLE_RICE,
            SeedOptionData.EGG,
            SeedOptionData.NO_ONION
    );

    public static final List<TestOption> FOOD_OPTIONS_WITHOUT_ADDON = List.of(
            SeedOptionData.LARGE_SIZE,
            SeedOptionData.MEDIUM_SPICY,
            SeedOptionData.WHITE_RICE,
            SeedOptionData.NO_ONION
    );

    public static final List<TestOption> FOOD_OPTIONS_WITHOUT_SPICE = List.of(
            SeedOptionData.NORMAL_SIZE,
            SeedOptionData.WHITE_RICE
    );

    public static final List<TestOption> FOOD_OPTIONS_WITH_INACTIVE = List.of(
            SeedOptionData.NORMAL_SIZE,
            SeedOptionData.MILD_SPICY,
            SeedOptionData.WHITE_RICE,
            SeedOptionData.ADD_ON_INACTIVE
    );

    public static final List<TestOption> DRINK_OPTIONS = List.of(
            SeedOptionData.COLD
    );

    public static final List<TestOption> DRINK_OPTIONS_INACTIVE = List.of(
            SeedOptionData.TEMP_INACTIVE
    );

    public static final List<TestOption> DRINK_OPTIONS_WITH_RICE_SIZE = List.of(
            SeedOptionData.COLD,
            SeedOptionData.NORMAL_SIZE
    );


    public static List<Integer> getOptionIdsList(List<TestOption> options) {
        List<Integer> optionDTOS = new ArrayList<>();
        for (TestOption option : options) {
            optionDTOS.add(option.id());
        }
        return optionDTOS;
    }

    public static OrderItemDTO getOrderItemDTO(TestProduct product, Integer productQuantity, List<Integer> optionIds) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setProductId(product.id());
        orderItemDTO.setQuantity(productQuantity);
        orderItemDTO.setOptionIds(optionIds);
        return orderItemDTO;
    }

    public static Integer calculateTotalPrice(TestProduct product, Integer productQuantity,
                                              List<TestOption> options) {
        Integer optionsTotal = 0;

        for (TestOption option : options) {
            optionsTotal += option.price();
        }

        return (product.price()+ optionsTotal) * productQuantity ;
    }

    public static OrderPageQueryDTO getOrderPageQueryDTO(Integer page, Integer pageSize, String orderNo, Integer status, LocalDate startDate, LocalDate endDate) {
        OrderPageQueryDTO orderPageQueryDTO = new OrderPageQueryDTO();
        orderPageQueryDTO.setPage(page);
        orderPageQueryDTO.setPageSize(pageSize);
        orderPageQueryDTO.setStartDate(startDate);
        orderPageQueryDTO.setEndDate(endDate);

        if(orderNo != null) {
            orderPageQueryDTO.setOrderNo(orderNo);
        }
        if(status != null) {
            orderPageQueryDTO.setStatus(status);
        }
        return orderPageQueryDTO;
    }

    public static OrderDetailVO getOrderDetailVO(TestOrder testOrder, TestProduct testProduct, List<TestOption> testOptions) {
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        BeanUtils.copyProperties(testOrder, orderDetailVO);
        orderDetailVO.setId(TestDataGenerator.getUniqueInt());

        OrderItemVO itemVO = new OrderItemVO();
        itemVO.setId(TestDataGenerator.getUniqueInt());
        itemVO.setOrderId(orderDetailVO.getId());
        itemVO.setProductId(testProduct.id());
        itemVO.setProductTitle(testProduct.title());
        itemVO.setProductType(testProduct.productType());
        itemVO.setProductPrice(testProduct.price());
        itemVO.setQuantity(1);
        itemVO.setLineTotal(10);

        List<OrderItemOptionVO> optionVOList = new ArrayList<>();
        for (TestOption option : testOptions) {
            OrderItemOptionVO optionVO = new OrderItemOptionVO();
            optionVO.setId(TestDataGenerator.getUniqueInt());
            optionVO.setOrderItemId(itemVO.getId());
            optionVO.setOptionId(option.id());
            optionVO.setOptionTitle(option.title());
            optionVO.setOptionPrice(option.price());
            optionVOList.add(optionVO);
        }
        itemVO.setOptions(optionVOList);
        orderDetailVO.setItems(List.of(itemVO));

        return orderDetailVO;
    }

    public static List<OrderableProductVO> getOrderableProductList(List<TestProduct> testProducts) {
        List<OrderableProductVO> productList = new ArrayList<>();
        for (TestProduct testProduct : testProducts) {
            OrderableProductVO productVO = new OrderableProductVO();
            BeanUtils.copyProperties(testProduct, productVO);
            productList.add(productVO);
        }
        return productList;
    }

    public static List<OrderableOptionVO> getOrderableOptionList(List<TestOption> testOptions) {
        List<OrderableOptionVO> optionList = new ArrayList<>();
        for (TestOption testOption : testOptions) {
            OrderableOptionVO optionVO = new OrderableOptionVO();
            BeanUtils.copyProperties(testOption, optionVO);
            optionList.add(optionVO);
        }
        return optionList;
    }


}
