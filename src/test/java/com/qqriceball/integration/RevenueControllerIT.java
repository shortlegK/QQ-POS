package com.qqriceball.integration;


import com.jayway.jsonpath.JsonPath;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OrderStatusEnum;
import com.qqriceball.enumeration.PeriodTypeEnum;
import com.qqriceball.model.dto.order.OrderCreateDTO;
import com.qqriceball.model.dto.order.OrderItemDTO;
import com.qqriceball.model.dto.order.OrderStatusDTO;
import com.qqriceball.model.vo.revenue.RevenueStatsVO;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.testData.order.SeedOrderData;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.utils.order.OrderTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RevenueControllerIT extends BaseIntegrationTest {


    @Test
    @DisplayName("[IT] 7001 getRevenueStatsByPeriodType - 使用一般權限帳號，查詢營收統計，應回傳 403")
    void testGetRevenueStatsByPeriodTypeWithStaffForbidden() throws Exception {

        mockMvc.perform(
                        get("/revenue")
                                .cookie(cookieStaff)
                                .param("periodType", PeriodTypeEnum.TODAY.getCode() + "")
                ).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[IT] 7001 getRevenueStatsByPeriodType - 使用管理員帳號，查詢營收統計，PeriodType = TODAY，應回傳 200 及資料")
    void testGetRevenueStatsByPeriodTypeTodaySuccess() throws Exception {

        List<Integer> optionList1 = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.FOOD_OPTIONS_WITH_OPTIONAL_ITEM);
        List<Integer> optionList2 = OrderTestDataFactory.getOptionIdsList(OrderTestDataFactory.DRINK_OPTIONS);

        Integer quantity = 20;
        OrderItemDTO orderItemDTO1 = OrderTestDataFactory.getOrderItemDTO(SeedProductData.MEAT_PRODUCT, quantity, optionList1);
        OrderItemDTO orderItemDTO2 = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, quantity, optionList2);

        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        LocalDateTime expectedPickupTime = LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.MINUTES);
        orderCreateDTO.setPickupTime(expectedPickupTime);
        orderCreateDTO.setItems(List.of(orderItemDTO1, orderItemDTO2));

        String jsonBody = objectMapper.writeValueAsString(orderCreateDTO);
        MvcResult createResult = mockMvc.perform(
                post("/orders/create")
                        .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
                .andReturn();

        String orderNo = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.orderNo");

        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(OrderStatusEnum.PICKED_UP.getCode());
        jsonBody = objectMapper.writeValueAsString(orderStatusDTO);
        mockMvc.perform(
                patch("/orders/{orderNo}/status",orderNo)
                        .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isOk());

        LocalDate today = LocalDate.now();
        LocalDate compareDate = today.minusDays(1);

        MvcResult result = mockMvc.perform(
                        get("/revenue")
                                .cookie(cookieManager)
                                .param("periodType", PeriodTypeEnum.TODAY.getCode() + "")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        Object dataObj = JsonPath.read(result.getResponse().getContentAsString(), "$.data");
        RevenueStatsVO revenueStats = objectMapper.convertValue(dataObj, RevenueStatsVO.class);

        assertAll(
                () -> assertEquals(today, revenueStats.getThisPeriod().getStartDate(),"thisPeriod 的 startDate 應為當日"),
                () -> assertEquals(today, revenueStats.getThisPeriod().getEndDate(),"thisPeriod 的 endDate 應為當日"),
                () -> assertEquals(compareDate, revenueStats.getComparePeriod().getStartDate(),"comparePeriod 的 startDate 應為昨日"),
                () -> assertEquals(compareDate, revenueStats.getComparePeriod().getEndDate(),"comparePeriod 的 endDate 應為昨日"),
                () -> assertEquals(1,revenueStats.getComparePeriod().getOrderCount(),"comparePeriod 的 orderCount 應為昨日訂單的筆數"),
                () -> assertEquals(SeedOrderData.orderPickedUpYesterday.total(),revenueStats.getComparePeriod().getActualRevenue(),"comparePeriod 的 actualRevenue 應為昨日訂單的總金額"),
                () -> assertEquals(SeedProductData.MEAT_PRODUCT.title(),revenueStats.getTopRiceBalls().get(0).getTitle(),"topRiceBalls 的第一名應為 MEAT_PRODUCT"),
                () -> assertTrue(revenueStats.getRiceTypeStats().get(0).getSalesCount() >= quantity),
                () -> assertEquals(SeedProductData.DRINK_PRODUCT.title(),revenueStats.getTopDrinks().get(0).getTitle(),"topDrinks 的第一名應為 DRINK_PRODUCT"),
                () -> assertTrue(revenueStats.getTopRiceBalls().get(0).getSalesCount() >= quantity),
                () -> assertEquals(SeedOptionData.EGG.title(),revenueStats.getTopAddOns().get(0).getTitle(),"topAddOns 的第一名應為 EGG"),
                () -> assertTrue(revenueStats.getTopDrinks().get(0).getSalesCount() >= quantity),
                () -> assertEquals(SeedOptionData.PURPLE_RICE.title(),revenueStats.getRiceTypeStats().get(0).getTitle(),"riceTypeStats 的第一名應為 PURPLE_RICE"),
                () -> assertTrue(revenueStats.getTopAddOns().get(0).getSalesCount() >= quantity)
        );
    }

    @Test
    @DisplayName("[IT] 7001 getRevenueStatsByPeriodType - 使用管理員帳號，查詢營收統計，PeriodType = THIS_WEEK，應回傳 200 及資料")
    void testGetRevenueStatsByPeriodTypeThisWeekSuccess() throws Exception {

        LocalDate expectedStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate expectedEnd = LocalDate.now().with(DayOfWeek.SUNDAY);
        LocalDate expectedCompareStart = expectedStart.minusWeeks(1);
        LocalDate expectedCompareEnd = expectedEnd.minusWeeks(1);

        MvcResult result = mockMvc.perform(
                        get("/revenue")
                                .cookie(cookieManager)
                                .param("periodType", PeriodTypeEnum.THIS_WEEK.getCode() + "")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        Object dataObj = JsonPath.read(result.getResponse().getContentAsString(), "$.data");
        RevenueStatsVO revenueStats = objectMapper.convertValue(dataObj, RevenueStatsVO.class);

        assertAll(
                () -> assertEquals(expectedStart, revenueStats.getThisPeriod().getStartDate(),"thisPeriod 的 startDate 應為本週的星期一"),
                () -> assertEquals(expectedEnd, revenueStats.getThisPeriod().getEndDate(),"thisPeriod 的 endDate 應為本週的星期日"),
                () -> assertEquals(expectedCompareStart, revenueStats.getComparePeriod().getStartDate(),"comparePeriod 的 startDate 應為上週的星期一"),
                () -> assertEquals(expectedCompareEnd, revenueStats.getComparePeriod().getEndDate(),"comparePeriod 的 endDate 應為上週的星期日"),
                // 因執行測試時昨日的測試訂單資料可能跨週
                () -> assertTrue(revenueStats.getComparePeriod().getOrderCount() >= 1,"comparePeriod 的 orderCount 應至少等於 1 筆"),
                () -> assertTrue(revenueStats.getComparePeriod().getActualRevenue() >= SeedOrderData.orderPickedUpLastWeek.total(),"comparePeriod 的 actualRevenue 應大於等於 orderPickedUpLastWeek 的金額"),

                () -> assertFalse(revenueStats.getTopRiceBalls().isEmpty(),"topRiceBalls 不應為空"),
                () -> assertTrue(revenueStats.getTopDrinks().isEmpty(),"topDrinks 應為空"),
                () -> assertFalse(revenueStats.getTopAddOns().isEmpty(),"topAddOns 不應為空"),
                () -> assertFalse(revenueStats.getRiceTypeStats().isEmpty(),"riceTypeStats 不應為空")
        );
    }

    @Test
    @DisplayName("[IT] 7001 getRevenueStatsByPeriodType - 使用管理員帳號，查詢營收統計，PeriodType = THIS_MONTH，應回傳 200 及資料")
    void testGetRevenueStatsByPeriodTypeThisMonthSuccess() throws Exception {

        LocalDate expectedStart = LocalDate.now().withDayOfMonth(1);
        LocalDate expectedEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        LocalDate expectedCompareStart = expectedStart.minusMonths(1).withDayOfMonth(1);
        LocalDate expectedCompareEnd = expectedCompareStart.withDayOfMonth(expectedCompareStart.lengthOfMonth());

        MvcResult result = mockMvc.perform(
                        get("/revenue")
                                .cookie(cookieManager)
                                .param("periodType", PeriodTypeEnum.THIS_MONTH.getCode() + "")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        Object dataObj = JsonPath.read(result.getResponse().getContentAsString(), "$.data");
        RevenueStatsVO revenueStats = objectMapper.convertValue(dataObj, RevenueStatsVO.class);

        assertAll(
                () -> assertEquals(expectedStart, revenueStats.getThisPeriod().getStartDate(),"thisPeriod 的 startDate 應為本週的星期一"),
                () -> assertEquals(expectedEnd, revenueStats.getThisPeriod().getEndDate(),"thisPeriod 的 endDate 應為本週的星期日"),
                () -> assertEquals(expectedCompareStart, revenueStats.getComparePeriod().getStartDate(),"comparePeriod 的 startDate 應為上週的星期一"),
                () -> assertEquals(expectedCompareEnd, revenueStats.getComparePeriod().getEndDate(),"comparePeriod 的 endDate 應為上週的星期日"),
                // 因執行測試時昨日的測試訂單資料可能跨月份
                () -> assertTrue(revenueStats.getComparePeriod().getOrderCount() >= 1,"comparePeriod 的 orderCount 應至少等於 1 筆"),
                () -> assertTrue(revenueStats.getComparePeriod().getActualRevenue() >= SeedOrderData.orderPickedUpLastMonth.total(),"comparePeriod 的 actualRevenue 應大於等於 orderPickedUpLastMonth 的金額"),

                () -> assertFalse(revenueStats.getTopRiceBalls().isEmpty(),"topRiceBalls 不應為空"),
                () -> assertTrue(revenueStats.getTopDrinks().isEmpty(),"topDrinks 應為空"),
                () -> assertFalse(revenueStats.getTopAddOns().isEmpty(),"topAddOns 不應為空"),
                () -> assertFalse(revenueStats.getRiceTypeStats().isEmpty(),"riceTypeStats 不應為空")
        );
    }
}
