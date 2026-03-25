package com.qqriceball.model.entity.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "訂單商品選項設定資料")

public class OrderItemOption {

    @Schema(description = "訂單商品選項設定 ID")
    private Integer id;

    @Schema(description = "訂單商品 ID")
    private Integer orderItemId;

    @Schema(description = "選項 ID")
    private Integer optionId;

    @Schema(description = "選項類型(0: 米飯種類, 1: 飯量, 2: 辣度, 3: 加料種類, 4:飲品溫度)")
    private Integer optionType;

    @Schema(description = "選項名稱")
    private String optionTitle;

    @Schema(description = "選項售價")
    private Integer optionPrice;

    @Schema(description = "訂購數量")
    private Integer quantity;

}
