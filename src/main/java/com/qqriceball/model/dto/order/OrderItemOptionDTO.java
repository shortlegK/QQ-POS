package com.qqriceball.model.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "訂單產品選項明細資料")
public class OrderItemOptionDTO {
    @NotNull(message = "細節選項 id 為必填")
    @Min(value = 1,message = "細節選項 id 設定錯誤")
    private Integer optionId;

    @NotNull
    @Min(value = 1, message = "選項數量至少為 1")
    private Integer quantity = 1;
}
