package com.qqriceball.model.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "訂單商品明細資料")
public class OrderItemDTO {

    @Schema(description = "商品 ID")
    @NotNull(message = "請輸入商品 ID")
    private Integer productId;

    @Schema(description = "商品訂購數量")
    @NotNull(message = "請輸入商品訂購數量")
    @Min(value = 1, message = "數量至少為 1")
    private Integer quantity;

    @Schema(description = "商品選項 ID 列表")
    @NotEmpty(message = "請輸入商品選項 ID")
    private List<Integer> optionIds;
}
