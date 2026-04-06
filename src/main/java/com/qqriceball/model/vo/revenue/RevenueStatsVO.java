package com.qqriceball.model.vo.revenue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = """
       營收統計資料
       
       僅統計訂單狀態為已領取的訂單
       """)
public class RevenueStatsVO {

    @Schema(description = "統計期間類型 (0:當日, 1:當周, 2:當月)")
    private int periodType;

    @Schema(description = "當期營收摘要資料")
    private PeriodSummaryVO thisPeriod;

    @Schema(description = "比較期間營收摘要資料")
    private PeriodSummaryVO comparePeriod;

    @Schema(description = "飯糰銷售排行(前五名)")
    private List<ProductRankVO> topRiceBalls;

    @Schema(description = "飲料銷售排行(前五名)")
    private List<ProductRankVO> topDrinks;

    @Schema(description = "加料選項排行(前五名)")
    private List<OptionRankVO> topAddOns;

    @Schema(description = "米飯種類銷售狀況")
    private List<OptionRankVO> riceTypeStats;

}
