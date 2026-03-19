package com.qqriceball.utils.order;

import com.qqriceball.model.dto.order.OrderItemDTO;
import com.qqriceball.model.dto.order.OrderItemOptionDTO;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.testData.option.TestOption;
import com.qqriceball.testData.product.TestProduct;

import java.util.ArrayList;
import java.util.List;

public class OrderTestDataFactory {

    public static final List<TestOption> FOOD_OPTIONS_WITH_ADD_ON = List.of(
            SeedOptionData.LARGE_SIZE,
            SeedOptionData.MEDIUM_SPICY,
            SeedOptionData.PURPLE_RICE,
            SeedOptionData.EGG
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


    public static List<OrderItemOptionDTO> toOptionDTOList(List<TestOption> options , Integer optionQuantity) {
        List<OrderItemOptionDTO> optionDTOS = new ArrayList<>();
        for (TestOption option : options) {
            OrderItemOptionDTO optionDTO = new OrderItemOptionDTO();
            optionDTO.setOptionId(option.id());
            optionDTO.setQuantity(optionQuantity);
            optionDTOS.add(optionDTO);
        }
        return optionDTOS;
    }

    public static OrderItemDTO getOrderItemDTO(TestProduct product, Integer productQuantity, List<OrderItemOptionDTO> options) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setProductId(product.id());
        orderItemDTO.setQuantity(productQuantity);
        orderItemDTO.setOptions(options);
        return orderItemDTO;
    }

    public static Integer calculateTotalPrice(TestProduct product, Integer productQuantity,
                                              List<TestOption> options, Integer optionQuantity) {
        Integer optionsTotal = 0;

        for (TestOption option : options) {
            optionsTotal += option.price() * optionQuantity;
        }

        return (product.price()+ optionsTotal) * productQuantity ;
    }

}
