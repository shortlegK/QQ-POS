package com.qqriceball.model.entity.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "訂單商品明細資料")

public class OrderItem {

    @Schema(description = "訂單商品 id")
    private Integer id;

    @Schema(description = "訂單 id")
    private Integer orderId;

    @Schema(description = "產品 id")
    private Integer productId;

    @Schema(description = "產品類型 (0: 葷食, 1: 素食, 2: 飲料)")
    private Integer productType;

    @Schema(description = "產品名稱")
    private String productTitle;

    @Schema(description = "產品售價")
    private Integer productPrice;

    @Schema(description = "訂購數量")
    private Integer quantity;

    @Schema(description = "項目小計")
    private Integer lineTotal;

}
