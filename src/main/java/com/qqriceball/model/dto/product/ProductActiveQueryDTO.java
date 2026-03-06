package com.qqriceball.model.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "產品分頁查詢資料")
public class ProductActiveQueryDTO implements Serializable {

    @Schema(description = "產品類型 (0: 葷食, 1: 素食, 2: 飲料)")
    @Min(value = 0, message = "產品類型設定錯誤")
    @Max(value = 2, message = "產品類型設定錯誤")
    private Integer productType;

}
