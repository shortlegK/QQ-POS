package com.qqriceball.model.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "訂單商品選項明細資料")
public class OrderItemOptionDTO {

    @Schema(description = "選項 ID")
    @NotNull(message = "請輸入選項 ID")
    private Integer optionId;

    @Schema(description = "選項數量")
    @NotNull
    @Min(value = 1, message = "選項數量至少為 1")
    private Integer quantity = 1;
}
