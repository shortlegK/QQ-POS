package com.qqriceball.model.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "訂單商品明細資料")
public class OrderItemDTO {

    @NotNull(message = "商品 id 為必填")
    @Min(value = 1 ,message = "商品 id 設定錯誤")
    private Integer productId;

    @NotNull(message = "數量為必填")
    @Min(value = 1, message = "數量至少為 1")
    private Integer quantity;

    @NotEmpty(message = "細節選項資料為必填")
    private List<@Valid OrderItemOptionDTO> options;
}
