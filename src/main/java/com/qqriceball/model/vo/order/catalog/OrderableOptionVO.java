package com.qqriceball.model.vo.order.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "可選擇的細節選項")
public class OrderableOptionVO {

    @Schema(description = "選項 ID")
    private Integer id;

    @Schema(description = "選項名稱")
    private String title;

    @Schema(description = "售價")
    private Integer price;

    @Schema(description = "是否為預設值 (0:否, 1:是)")
    private Integer isDefault;

}
