package com.qqriceball.model.vo.revenue;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "選項排行資料")
public class OptionRankVO {

    @Schema(description = "選項名稱")
    private String title;

    @Schema(description = "銷售數量")
    private Integer salesCount;
}
