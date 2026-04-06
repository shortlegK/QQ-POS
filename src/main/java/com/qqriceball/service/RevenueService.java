package com.qqriceball.service;

import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OptionTypeEnum;
import com.qqriceball.enumeration.PeriodTypeEnum;
import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.mapper.RevenueMapper;
import com.qqriceball.model.dto.revenue.RevenueDTO;
import com.qqriceball.model.vo.revenue.PeriodSummaryVO;
import com.qqriceball.model.vo.revenue.RevenueStatsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Slf4j
@Service
public class RevenueService {

    private final RevenueMapper revenueMapper;

    @Autowired
    public RevenueService(RevenueMapper revenueMapper) {
        this.revenueMapper = revenueMapper;
    }

    public RevenueStatsVO getByPeriodType(RevenueDTO revenueDTO) {

        RevenueStatsVO revenueStatsVO = new RevenueStatsVO();
        // 設定查詢期間相關資訊
        this.setPeriodDates(revenueStatsVO, revenueDTO.getPeriodType());

        // 查詢營收統計資料
        PeriodSummaryVO thisPeriodResult = revenueMapper.getSummaryByPeriod(revenueStatsVO.getThisPeriod());
        revenueStatsVO.getThisPeriod().setOrderCount(thisPeriodResult.getOrderCount());
        revenueStatsVO.getThisPeriod().setActualRevenue(thisPeriodResult.getActualRevenue());
        revenueStatsVO.getThisPeriod().setAvgRevenuePerOrder(thisPeriodResult.getAvgRevenuePerOrder());

        PeriodSummaryVO comparePeriodResult = revenueMapper.getSummaryByPeriod(revenueStatsVO.getComparePeriod());
        revenueStatsVO.getComparePeriod().setOrderCount(comparePeriodResult.getOrderCount());
        revenueStatsVO.getComparePeriod().setActualRevenue(comparePeriodResult.getActualRevenue());
        revenueStatsVO.getComparePeriod().setAvgRevenuePerOrder(comparePeriodResult.getAvgRevenuePerOrder());

        // 查詢銷售排行資料
        revenueStatsVO.setTopRiceBalls(revenueMapper.getTopProductByType(revenueStatsVO.getThisPeriod(),
                ProductTypeEnum.MEAT.getCode(), ProductTypeEnum.VEGAN.getCode()));
        revenueStatsVO.setTopDrinks(revenueMapper.getTopProductByType(revenueStatsVO.getThisPeriod(),
                ProductTypeEnum.DRINKS.getCode()));

        // 查詢選項排行資料
        revenueStatsVO.setTopAddOns(revenueMapper.getTopOptionByType(revenueStatsVO.getThisPeriod(),
                OptionTypeEnum.ADD_ON.getCode()));
        revenueStatsVO.setRiceTypeStats(revenueMapper.getTopOptionByType(revenueStatsVO.getThisPeriod(),
                OptionTypeEnum.RICE_TYPE.getCode()));

        return revenueStatsVO;
    }


    private void setPeriodDates(RevenueStatsVO revenueStatsVO, int periodType) {
        LocalDate today = LocalDate.now();

        PeriodTypeEnum period = PeriodTypeEnum.getByCode(periodType);
        if (period == null) {
            log.warn("未知的統計期間類型: {}", periodType);
            throw new BadRequestArgsException(MessageEnum.PERIOD_TYPE_INVALID);
        }

        switch (period) {
            case TODAY -> {
                PeriodSummaryVO thisPeriod = new PeriodSummaryVO();
                thisPeriod.setStartDate(today);
                thisPeriod.setEndDate(today);

                PeriodSummaryVO comparePeriod = new PeriodSummaryVO();
                comparePeriod.setStartDate(today.minusDays(1));
                comparePeriod.setEndDate(today.minusDays(1));

                revenueStatsVO.setPeriodType(periodType);
                revenueStatsVO.setThisPeriod(thisPeriod);
                revenueStatsVO.setComparePeriod(comparePeriod);
            }
            case THIS_WEEK ->
            {
                PeriodSummaryVO thisPeriod = new PeriodSummaryVO();
                thisPeriod.setStartDate(today.with(DayOfWeek.MONDAY));
                thisPeriod.setEndDate(today.with(DayOfWeek.SUNDAY));

                PeriodSummaryVO comparePeriod = new PeriodSummaryVO();
                comparePeriod.setStartDate(today.minusWeeks(1)
                        .with(DayOfWeek.MONDAY));
                comparePeriod.setEndDate(today.minusWeeks(1)
                        .with(DayOfWeek.SUNDAY));

                revenueStatsVO.setPeriodType(periodType);
                revenueStatsVO.setThisPeriod(thisPeriod);
                revenueStatsVO.setComparePeriod(comparePeriod);
            }
            case THIS_MONTH ->
            {
                PeriodSummaryVO thisPeriod = new PeriodSummaryVO();
                thisPeriod.setStartDate(today.withDayOfMonth(1));
                thisPeriod.setEndDate(today.withDayOfMonth(today.lengthOfMonth()));

                PeriodSummaryVO comparePeriod = new PeriodSummaryVO();
                comparePeriod.setStartDate(today.minusMonths(1).withDayOfMonth(1));
                comparePeriod.setEndDate(today.minusMonths(1)
                        .withDayOfMonth(today.minusMonths(1).lengthOfMonth()));

                revenueStatsVO.setPeriodType(periodType);
                revenueStatsVO.setThisPeriod(thisPeriod);
                revenueStatsVO.setComparePeriod(comparePeriod);
            }
        }
    }

}
