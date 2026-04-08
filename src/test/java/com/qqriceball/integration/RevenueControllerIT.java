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

        int expectedTopQuantity = 20;
        OrderItemDTO orderItemDTO1 = OrderTestDataFactory.getOrderItemDTO(SeedProductData.VEG_PRODUCT, expectedTopQuantity, optionList1);
        OrderItemDTO orderItemDTO2 = OrderTestDataFactory.getOrderItemDTO(SeedProductData.DRINK_PRODUCT, expectedTopQuantity, optionList2);
        String expectedTopTitle = SeedProductData.VEG_PRODUCT.title();
        String expectedTopRiceTypeTitle = SeedOptionData.PURPLE_RICE.title();

        int expectedSecondQuantity = 1;
        String expectedSecondTitle = SeedProductData.MEAT_PRODUCT.title();
        String expectedSecondRicyTypeTitle = SeedOptionData.WHITE_RICE.title();

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
        int newOrderTotal = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.total");

        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setStatus(OrderStatusEnum.PICKED_UP.getCode());
        jsonBody = objectMapper.writeValueAsString(orderStatusDTO);
        mockMvc.perform(
                patch("/orders/{orderNo}/status",orderNo)
                        .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isOk());

        LocalDate expectedToday = LocalDate.now();
        int expectedTodayOrderCount = 2; // 包含 SeedOrderData.orderPickedUp 和此測試案例建立的新訂單
        int expectedTodayRevenue = SeedOrderData.orderPickedUp.total() + newOrderTotal;

        LocalDate compareDate = expectedToday.minusDays(1);
        int expectedCompareOrderCount = 1;
        int expectedCompareRevenue = SeedOrderData.orderPickedUpYesterday.total();

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
                () -> assertEquals(expectedToday, revenueStats.getThisPeriod().getStartDate(),"thisPeriod 的 startDate 應為當日"),
                () -> assertEquals(expectedToday, revenueStats.getThisPeriod().getEndDate(),"thisPeriod 的 endDate 應為當日"),
                () -> assertEquals(expectedTodayOrderCount, revenueStats.getThisPeriod().getOrderCount(),"thisPeriod 的 orderCount 應為當日訂單的筆數"),
                () -> assertEquals(expectedTodayRevenue, revenueStats.getThisPeriod().getActualRevenue(),"thisPeriod 的 actualRevenue 應為當日訂單的總金額"),

                () -> assertEquals(compareDate, revenueStats.getComparePeriod().getStartDate(),"comparePeriod 的 startDate 應為昨日"),
                () -> assertEquals(compareDate, revenueStats.getComparePeriod().getEndDate(),"comparePeriod 的 endDate 應為昨日"),
                () -> assertEquals(expectedCompareOrderCount,revenueStats.getComparePeriod().getOrderCount(),"comparePeriod 的 orderCount 應為昨日訂單的筆數"),
                () -> assertEquals(expectedCompareRevenue,revenueStats.getComparePeriod().getActualRevenue(),"comparePeriod 的 actualRevenue 應為昨日訂單的總金額"),

                () -> assertEquals(expectedTopTitle, revenueStats.getTopRiceBalls().get(0).getTitle(),"topRiceBalls 的第一名應為 VEG_PRODUCT"),
                () -> assertEquals(expectedTopQuantity, revenueStats.getTopRiceBalls().get(0).getSalesCount(), "topRiceBalls 的第一名銷售數量應為測試案例建立的訂購數量"),
                () -> assertEquals(expectedSecondTitle, revenueStats.getTopRiceBalls().get(1).getTitle(),"topRiceBalls 的第二名應為 MEAT_PRODUCT"),
                () -> assertEquals(expectedSecondQuantity, revenueStats.getTopRiceBalls().get(1).getSalesCount(),"topRiceBalls 的第二名銷售數量應為測試種子資料的訂購數量"),

                () -> assertEquals(expectedTopRiceTypeTitle, revenueStats.getRiceTypeStats().get(0).getTitle(),"riceTypeStats 的第一名應為 PURPLE_RICE"),
                () -> assertEquals(expectedTopQuantity, revenueStats.getRiceTypeStats().get(0).getSalesCount(),"riceTypeStats 的第一名銷售數量應為測試案例建立的訂購數量"),
                () -> assertEquals(expectedSecondRicyTypeTitle, revenueStats.getRiceTypeStats().get(1).getTitle(),"riceTypeStats 的第二名應為 WHITE_RICE"),
                () -> assertEquals(expectedSecondQuantity, revenueStats.getRiceTypeStats().get(1).getSalesCount(),"riceTypeStats 的第二名銷售數量應為測試種子資料的訂購數量"),

                () -> assertEquals(SeedOptionData.EGG.title(),revenueStats.getTopAddOns().get(0).getTitle(),"topAddOns 的第一名應為 EGG"),
                () -> assertEquals(expectedTopQuantity, revenueStats.getTopAddOns().get(0).getSalesCount(),"topAddOns 的第一名銷售數量應為測試案例建立的訂購數量"),
                () -> assertEquals(SeedProductData.DRINK_PRODUCT.title(), revenueStats.getTopDrinks().get(0).getTitle(),"topDrinks 的第一名應為 DRINK_PRODUCT"),
                () -> assertEquals(expectedTopQuantity, revenueStats.getTopDrinks().get(0).getSalesCount(), "topDrinks 的第一名銷售數量應為測試案例建立的訂購數量")
        );
    }

    @Test
    @DisplayName("[IT] 7001 getRevenueStatsByPeriodType - 使用管理員帳號，查詢營收統計，PeriodType = THIS_WEEK，應回傳 200 及資料")
    void testGetRevenueStatsByPeriodTypeThisWeekSuccess() throws Exception {

        LocalDate expectedThisWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate expectedThisWeekEnd = LocalDate.now().with(DayOfWeek.SUNDAY);
        int expectedThisWeekOrderCount;
        int expectedThisWeekRevenue;

        LocalDate expectedCompareStart = expectedThisWeekStart.minusWeeks(1);
        LocalDate expectedCompareEnd = expectedThisWeekEnd.minusWeeks(1);
        int expectedCompareOrderCount;
        int expectedCompareRevenue;

        // 因執行測試時，昨日可能跨週
        LocalDate yesterday = LocalDate.now().minusDays(1);
        if (yesterday.isBefore(expectedThisWeekStart)){
            expectedThisWeekOrderCount = 1;
            expectedThisWeekRevenue = SeedOrderData.orderPickedUp.total();
            expectedCompareOrderCount = 2;
            expectedCompareRevenue = SeedOrderData.orderPickedUpYesterday.total() + SeedOrderData.orderPickedUpLastWeek.total();
        } else {
            expectedThisWeekOrderCount = 2;
            expectedThisWeekRevenue = SeedOrderData.orderPickedUp.total() + SeedOrderData.orderPickedUpYesterday.total();
            expectedCompareOrderCount = 1;
            expectedCompareRevenue = SeedOrderData.orderPickedUpLastWeek.total();
        }

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
                () -> assertEquals(expectedThisWeekStart, revenueStats.getThisPeriod().getStartDate(),"thisPeriod 的 startDate 應為本週的星期一"),
                () -> assertEquals(expectedThisWeekEnd, revenueStats.getThisPeriod().getEndDate(),"thisPeriod 的 endDate 應為本週的星期日"),
                () -> assertEquals(expectedThisWeekOrderCount, revenueStats.getThisPeriod().getOrderCount(),"thisPeriod 的 orderCount 應為本週訂單的筆數"),
                () -> assertEquals(expectedThisWeekRevenue, revenueStats.getThisPeriod().getActualRevenue(),"thisPeriod 的 actualRevenue 應為本週訂單的總金額"),
                () -> assertEquals(expectedCompareStart, revenueStats.getComparePeriod().getStartDate(),"comparePeriod 的 startDate 應為上週的星期一"),
                () -> assertEquals(expectedCompareEnd, revenueStats.getComparePeriod().getEndDate(),"comparePeriod 的 endDate 應為上週的星期日"),
                () -> assertEquals(expectedCompareOrderCount, revenueStats.getComparePeriod().getOrderCount(),"comparePeriod 的 orderCount 應為上週訂單的筆數"),
                () -> assertEquals(expectedCompareRevenue, revenueStats.getComparePeriod().getActualRevenue(),"comparePeriod 的 actualRevenue 應為上週訂單的總金額"),
                () -> assertFalse(revenueStats.getTopRiceBalls().isEmpty(),"topRiceBalls 不應為空"),
                () -> assertTrue(revenueStats.getTopDrinks().isEmpty(),"topDrinks 應為空"),
                () -> assertFalse(revenueStats.getTopAddOns().isEmpty(),"topAddOns 不應為空"),
                () -> assertFalse(revenueStats.getRiceTypeStats().isEmpty(),"riceTypeStats 不應為空")
        );
    }

    @Test
    @DisplayName("[IT] 7001 getRevenueStatsByPeriodType - 使用管理員帳號，查詢營收統計，PeriodType = THIS_MONTH，應回傳 200 及資料")
    void testGetRevenueStatsByPeriodTypeThisMonthSuccess() throws Exception {

        LocalDate expectedThisMonthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate expectedThisMonthEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        int expectedThisMonthOrderCount;
        int expectedThisMonthRevenue;

        LocalDate expectedCompareStart = expectedThisMonthStart.minusMonths(1).withDayOfMonth(1);
        LocalDate expectedCompareEnd = expectedCompareStart.withDayOfMonth(expectedCompareStart.lengthOfMonth());
        int expectedCompareOrderCount;
        int expectedCompareRevenue;

        // 因執行測試時，昨日、上週的測試訂單資料可能跨月份
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);

        if (yesterday.isBefore(expectedThisMonthStart)){
            expectedThisMonthOrderCount = 1;
            expectedThisMonthRevenue = SeedOrderData.orderPickedUp.total();
            expectedCompareOrderCount = 3;
            expectedCompareRevenue = SeedOrderData.orderPickedUpLastMonth.total() + SeedOrderData.orderPickedUpYesterday.total() + SeedOrderData.orderPickedUpLastWeek.total();
        }else if(lastWeek.isBefore(expectedThisMonthStart)){
            expectedThisMonthOrderCount = 2;
            expectedThisMonthRevenue = SeedOrderData.orderPickedUp.total() + SeedOrderData.orderPickedUpYesterday.total();
            expectedCompareOrderCount = 2;
            expectedCompareRevenue = SeedOrderData.orderPickedUpLastMonth.total() + SeedOrderData.orderPickedUpLastWeek.total();
        } else {
            expectedThisMonthOrderCount = 3;
            expectedThisMonthRevenue = SeedOrderData.orderPickedUp.total() + SeedOrderData.orderPickedUpYesterday.total() + SeedOrderData.orderPickedUpLastWeek.total();
            expectedCompareOrderCount = 1;
            expectedCompareRevenue = SeedOrderData.orderPickedUpLastMonth.total();
        }

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
                () -> assertEquals(expectedThisMonthStart, revenueStats.getThisPeriod().getStartDate(),"thisPeriod 的 startDate 應為本週的星期一"),
                () -> assertEquals(expectedThisMonthEnd, revenueStats.getThisPeriod().getEndDate(),"thisPeriod 的 endDate 應為本週的星期日"),
                () -> assertEquals(expectedThisMonthOrderCount, revenueStats.getThisPeriod().getOrderCount(),"thisPeriod 的 orderCount 應為本月訂單的筆數"),
                () -> assertEquals(expectedThisMonthRevenue, revenueStats.getThisPeriod().getActualRevenue(),"thisPeriod 的 actualRevenue 應為本月訂單的總金額"),
                () -> assertEquals(expectedCompareStart, revenueStats.getComparePeriod().getStartDate(),"comparePeriod 的 startDate 應為上週的星期一"),
                () -> assertEquals(expectedCompareEnd, revenueStats.getComparePeriod().getEndDate(),"comparePeriod 的 endDate 應為上週的星期日"),
                () -> assertEquals(expectedCompareOrderCount, revenueStats.getComparePeriod().getOrderCount(),"comparePeriod 的 orderCount 應為上月訂單的筆數"),
                () -> assertEquals(expectedCompareRevenue, revenueStats.getComparePeriod().getActualRevenue(),"comparePeriod 的 actualRevenue 應為上月訂單的總金額"),
                () -> assertFalse(revenueStats.getTopRiceBalls().isEmpty(),"topRiceBalls 不應為空"),
                () -> assertTrue(revenueStats.getTopDrinks().isEmpty(),"topDrinks 應為空"),
                () -> assertFalse(revenueStats.getTopAddOns().isEmpty(),"topAddOns 不應為空"),
                () -> assertFalse(revenueStats.getRiceTypeStats().isEmpty(),"riceTypeStats 不應為空")
        );
    }
}
