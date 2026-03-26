package com.qqriceball.model.vo.order.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "可訂購商品")
public class OrderableProductVO {

    @Schema(description = "商品 ID")
    private Integer id;

    @Schema(description = "商品名稱")
    private String title;

    @Schema(description = "商品類型 (0:葷食, 1:素食, 2:飲品)")
    private Integer productType;

    @Schema(description = "售價")
    private Integer price;

}
