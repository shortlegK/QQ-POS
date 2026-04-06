package com.qqriceball.model.vo.revenue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "期間營收摘要資料")
public class PeriodSummaryVO {

    @Schema(description = "開始日期")
    private LocalDate startDate;

    @Schema(description = "結束日期")
    private LocalDate endDate;

    @Schema(description = "實際收入")
    private Integer actualRevenue;

    @Schema(description = "訂單數量")
    private Integer orderCount;

    @Schema(description = "平均每單收入")
    private Double avgRevenuePerOrder;

}
