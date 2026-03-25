package com.qqriceball.model.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "訂單商品明細資料")
public class OrderItemVO {

    @Schema(description = "訂單商品 ID")
    private Integer id;

    @Schema(description = "訂單 ID")
    private Integer orderId;

    @Schema(description = "產品 ID")
    private Integer productId;
    
    @Schema(description = "商品名稱")
    private String productTitle;

    @Schema(description = "商品類型 (0 - 葷飯糰,1 - 素飯糰 ,2 - 飲品 )")
    private Integer productType;

    @Schema(description = "商品價格")
    private Integer productPrice;

    @Schema(description = "訂購數量")
    private Integer quantity;

    @Schema(description = "商品明細小計")
    private Integer lineTotal;

    @Schema(description = "訂單商品明細")
    private List<OrderItemOptionVO> options;

}
