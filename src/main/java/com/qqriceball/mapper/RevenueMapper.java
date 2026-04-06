package com.qqriceball.mapper;

import com.qqriceball.model.vo.revenue.OptionRankVO;
import com.qqriceball.model.vo.revenue.PeriodSummaryVO;
import com.qqriceball.model.vo.revenue.ProductRankVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RevenueMapper {

    PeriodSummaryVO getSummaryByPeriod(PeriodSummaryVO periodSummaryVO);

    List<ProductRankVO> getTopProductByType(@Param("periodSummaryVO") PeriodSummaryVO periodSummaryVO,
                                                   @Param("productTypes") int... productTypes);

    List<OptionRankVO> getTopOptionByType(@Param("periodSummaryVO") PeriodSummaryVO periodSummaryVO,
                                                 @Param("optionType") int optionType);

}
