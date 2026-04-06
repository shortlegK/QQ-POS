package com.qqriceball.unit.service;


import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.PeriodTypeEnum;
import com.qqriceball.mapper.RevenueMapper;
import com.qqriceball.model.dto.revenue.RevenueDTO;
import com.qqriceball.model.vo.revenue.PeriodSummaryVO;
import com.qqriceball.service.RevenueService;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.utils.revenue.RevenueTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RevenueServiceTest {

    @Mock
    private RevenueMapper revenueMapper;

    @InjectMocks
    private RevenueService revenueService;

    @Test
    @DisplayName("[Unit] RevenueService.getByPeriodType() - 查詢營收統計，PeriodType TODAY，應呼叫 RevenueMapper 傳入參數")
    void testGetByPeriodTypeTodaySuccess() {

        LocalDate expectedToday = LocalDate.now();
        LocalDate expectedCompareDay = LocalDate.now().minusDays(1);

        RevenueDTO revenueDTO = RevenueTestDataFactory.getRevenueDTO(PeriodTypeEnum.TODAY.getCode());

        when(revenueMapper.getSummaryByPeriod(any(PeriodSummaryVO.class))).thenReturn(RevenueTestDataFactory.getPeriodSummary());
        when(revenueMapper.getTopProductByType(any(PeriodSummaryVO.class), anyInt(), anyInt()))
                .thenReturn(RevenueTestDataFactory.getProductRankList(SeedProductData.MEAT_PRODUCT));
        when(revenueMapper.getTopOptionByType(any(PeriodSummaryVO.class), anyInt()))
                .thenReturn(RevenueTestDataFactory.getOptionRankList(SeedOptionData.EGG));

        revenueService.getByPeriodType(revenueDTO);

        ArgumentCaptor<PeriodSummaryVO> summaryCaptor = ArgumentCaptor.forClass(PeriodSummaryVO.class);
        verify(revenueMapper,times(2)).getSummaryByPeriod(summaryCaptor.capture());
        List<PeriodSummaryVO> summaryCaptoredRusults = summaryCaptor.getAllValues();

        PeriodSummaryVO thisPeriodCaptured = summaryCaptoredRusults.get(0);
        PeriodSummaryVO comparePeriodCaptured = summaryCaptoredRusults.get(1);

        verify(revenueMapper).getTopProductByType(any(PeriodSummaryVO.class), anyInt(), anyInt());
        verify(revenueMapper).getTopProductByType(any(PeriodSummaryVO.class), anyInt());
        verify(revenueMapper,times(2)).getTopOptionByType(any(PeriodSummaryVO.class), anyInt());

        assertAll(
                () -> assertEquals(expectedToday, thisPeriodCaptured.getStartDate(), "thisPeriod 的 startDate 應為當日"),
                () -> assertEquals(expectedToday, thisPeriodCaptured.getEndDate(), "thisPeriod 的 endDate 應為當日"),
                () -> assertEquals(expectedCompareDay, comparePeriodCaptured.getStartDate(), "comparePeriod 的 startDate 應為昨日"),
                () -> assertEquals(expectedCompareDay, comparePeriodCaptured.getEndDate(), "comparePeriod 的 endDate 應為昨日")
        );
    }

    @Test
    @DisplayName("[Unit] RevenueService.getByPeriodType() - 查詢營收統計，PeroidType = THIS_WEEK，應呼叫 RevenueMapper 傳入參數")
    void testGetByPeriodTypeThisWeekSuccess() {

        LocalDate expectedThisStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate expectedThisEnd = LocalDate.now().with(DayOfWeek.SUNDAY);
        LocalDate expectedCompareStart = expectedThisStart.minusWeeks(1);
        LocalDate expectedCompareEnd = expectedThisEnd.minusWeeks(1);

        RevenueDTO revenueDTO = RevenueTestDataFactory.getRevenueDTO(PeriodTypeEnum.THIS_WEEK.getCode());

        when(revenueMapper.getSummaryByPeriod(any(PeriodSummaryVO.class))).thenReturn(RevenueTestDataFactory.getPeriodSummary());
        when(revenueMapper.getTopProductByType(any(PeriodSummaryVO.class), anyInt(), anyInt()))
                .thenReturn(RevenueTestDataFactory.getProductRankList(SeedProductData.MEAT_PRODUCT));
        when(revenueMapper.getTopOptionByType(any(PeriodSummaryVO.class), anyInt()))
                .thenReturn(RevenueTestDataFactory.getOptionRankList(SeedOptionData.EGG));

        revenueService.getByPeriodType(revenueDTO);

        ArgumentCaptor<PeriodSummaryVO> summaryCaptor = ArgumentCaptor.forClass(PeriodSummaryVO.class);
        verify(revenueMapper,times(2)).getSummaryByPeriod(summaryCaptor.capture());
        List<PeriodSummaryVO> summaryCaptoredRusults = summaryCaptor.getAllValues();

        PeriodSummaryVO thisPeriodCaptured = summaryCaptoredRusults.get(0);
        PeriodSummaryVO comparePeriodCaptured = summaryCaptoredRusults.get(1);

        verify(revenueMapper).getTopProductByType(any(PeriodSummaryVO.class), anyInt(), anyInt());
        verify(revenueMapper).getTopProductByType(any(PeriodSummaryVO.class), anyInt());
        verify(revenueMapper,times(2)).getTopOptionByType(any(PeriodSummaryVO.class), anyInt());

        assertAll(
                () -> assertEquals(expectedThisStart, thisPeriodCaptured.getStartDate(), "thisPeriod 的 startDate 應為當週一"),
                () -> assertEquals(expectedThisEnd, thisPeriodCaptured.getEndDate(), "thisPeriod 的 endDate 應為當週日"),
                () -> assertEquals(expectedCompareStart, comparePeriodCaptured.getStartDate(), "comparePeriod 的 startDate 應為上週一"),
                () -> assertEquals(expectedCompareEnd, comparePeriodCaptured.getEndDate(), "comparePeriod 的 endDate 應為上週日")
        );
    }

    @Test
    @DisplayName("[Unit] RevenueService.getByPeriodType() - 查詢本月營收統計，應呼叫 RevenueMapper 傳入參數")
    void testGetByPeriodTypeThisMonthSuccess() {

        LocalDate expectedThisStart = LocalDate.now().withDayOfMonth(1);
        LocalDate expectedThisEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        LocalDate expectedCompareStart = expectedThisStart.minusMonths(1);
        LocalDate expectedCompareEnd = expectedCompareStart.withDayOfMonth(expectedCompareStart.lengthOfMonth());

        RevenueDTO revenueDTO = RevenueTestDataFactory.getRevenueDTO(PeriodTypeEnum.THIS_MONTH.getCode());

        when(revenueMapper.getSummaryByPeriod(any(PeriodSummaryVO.class))).thenReturn(RevenueTestDataFactory.getPeriodSummary());
        when(revenueMapper.getTopProductByType(any(PeriodSummaryVO.class), anyInt(), anyInt()))
                .thenReturn(RevenueTestDataFactory.getProductRankList(SeedProductData.MEAT_PRODUCT));
        when(revenueMapper.getTopOptionByType(any(PeriodSummaryVO.class), anyInt()))
                .thenReturn(RevenueTestDataFactory.getOptionRankList(SeedOptionData.EGG));

        revenueService.getByPeriodType(revenueDTO);

        ArgumentCaptor<PeriodSummaryVO> summaryCaptor = ArgumentCaptor.forClass(PeriodSummaryVO.class);
        verify(revenueMapper,times(2)).getSummaryByPeriod(summaryCaptor.capture());
        List<PeriodSummaryVO> summaryCaptoredRusults = summaryCaptor.getAllValues();

        PeriodSummaryVO thisPeriodCaptured = summaryCaptoredRusults.get(0);
        PeriodSummaryVO comparePeriodCaptured = summaryCaptoredRusults.get(1);

        verify(revenueMapper).getTopProductByType(any(PeriodSummaryVO.class), anyInt(), anyInt());
        verify(revenueMapper).getTopProductByType(any(PeriodSummaryVO.class), anyInt());
        verify(revenueMapper,times(2)).getTopOptionByType(any(PeriodSummaryVO.class), anyInt());

        assertAll(
                () -> assertEquals(expectedThisStart, thisPeriodCaptured.getStartDate(), "thisPeriod 的 startDate 應為當月第一天"),
                () -> assertEquals(expectedThisEnd, thisPeriodCaptured.getEndDate(), "thisPeriod 的 endDate 應為當月最後一天"),
                () -> assertEquals(expectedCompareStart, comparePeriodCaptured.getStartDate(), "comparePeriod 的 startDate 應為上月第一天"),
                () -> assertEquals(expectedCompareEnd, comparePeriodCaptured.getEndDate(), "comparePeriod 的 endDate 應為上月最後一天")
        );
    }

    @Test
    @DisplayName("[Unit] RevenueService.getByPeriodType() - 查詢營收統計，PeriodType 不合法，應拋出 BadRequestArgsException")
    void testGetByPeriodTypeInvalidPeriodType() {

        RevenueDTO revenueDTO = RevenueTestDataFactory.getRevenueDTO(PeriodTypeEnum.values().length);

        BadRequestArgsException exception = assertThrows(BadRequestArgsException.class,
                () -> revenueService.getByPeriodType(revenueDTO));

        assertEquals(MessageEnum.PERIOD_TYPE_INVALID.getMessage(), exception.getMessage(), "應拋出 PERIOD_TYPE_INVALID 的錯誤訊息");
    }
}
