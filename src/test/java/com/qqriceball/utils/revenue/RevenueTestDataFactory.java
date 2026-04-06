package com.qqriceball.utils.revenue;


import com.qqriceball.model.dto.revenue.RevenueDTO;
import com.qqriceball.model.vo.revenue.OptionRankVO;
import com.qqriceball.model.vo.revenue.PeriodSummaryVO;
import com.qqriceball.model.vo.revenue.ProductRankVO;
import com.qqriceball.model.vo.revenue.RevenueStatsVO;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.testData.option.TestOption;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.testData.product.TestProduct;


import java.time.LocalDate;
import java.util.List;

public class RevenueTestDataFactory {

    public static RevenueDTO getRevenueDTO(Integer periodType) {
        RevenueDTO revenueDTO = new RevenueDTO();
        revenueDTO.setPeriodType(periodType);

        return revenueDTO;
    }

    public static RevenueStatsVO getRevenueStatsVO() {

        LocalDate today = LocalDate.now();
        RevenueStatsVO revenueStatsVO = new RevenueStatsVO();

        revenueStatsVO.setThisPeriod(getPeriodSummary());
        revenueStatsVO.getThisPeriod().setStartDate(today);
        revenueStatsVO.getThisPeriod().setEndDate(today);

        revenueStatsVO.setComparePeriod(getPeriodSummary());
        revenueStatsVO.getComparePeriod().setStartDate(today.minusDays(1));
        revenueStatsVO.getComparePeriod().setEndDate(today.minusDays(1));

        revenueStatsVO.setTopRiceBalls(getProductRankList(SeedProductData.MEAT_PRODUCT));
        revenueStatsVO.setTopDrinks(getProductRankList(SeedProductData.DRINK_PRODUCT));
        revenueStatsVO.setTopAddOns(getOptionRankList(SeedOptionData.EGG));
        revenueStatsVO.setRiceTypeStats(getOptionRankList(SeedOptionData.WHITE_RICE));

        return revenueStatsVO;
    }

    public static PeriodSummaryVO getPeriodSummary() {
        PeriodSummaryVO periodSummaryVO = new PeriodSummaryVO();
        periodSummaryVO.setActualRevenue(1000);
        periodSummaryVO.setOrderCount(10);
        periodSummaryVO.setAvgRevenuePerOrder(100.0);
        return periodSummaryVO;
    }

    public static List<ProductRankVO> getProductRankList(TestProduct productData) {

        ProductRankVO product = new ProductRankVO();
        product.setTitle(productData.title());
        product.setSalesCount(50);

        return List.of(product);
    }

    public static List<OptionRankVO> getOptionRankList(TestOption optionData) {

        OptionRankVO option = new OptionRankVO();
        option.setTitle(optionData.title());
        option.setSalesCount(30);

        return List.of(option);
    }
}
