package com.qqriceball.model.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "訂單商品細節選項資料")
public class OrderItemOptionVO {

    @Schema(description = "訂單選項 id")
    private Integer id;

    @Schema(description = "選項資料 id")
    private Integer optionId;

    @Schema(description = "選項名稱")
    private String optionTitle;

    @Schema(description = "選項價格")
    private Integer optionPrice;

    @Schema(description = "選項類型(0: 米飯種類, 1: 飯量, 2: 辣度, 3: 加料種類, 4:飲品溫度)")
    private Integer optionType;

    @Schema(description = "選項訂購數量")
    private Integer quantity;

}
