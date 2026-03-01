package com.qqriceball.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "產品建立/編輯資料")
public class ProductDTO {

    private Integer id;

    @Schema(description = "名稱")
    @NotBlank(message = "名稱為必填")
    private String title;

    @Schema(description = "產品類型 (0: 葷食, 1: 素食, 2: 飲料)")
    @NotNull(message = "產品類型為必填")
    @Min(value = 0, message = "產品類型設定錯誤")
    @Max(value = 2, message = "產品類型設定錯誤")
    private Integer productType;

    @Schema(description = "售價")
    @NotNull(message = "售價為必填")
    @Min(value = 1, message = "售價不得小於 1")
    private Integer price;

    @Schema(description = "啟用狀態(0:停用 / 1:啟用)")
    @NotNull(message = "啟用狀態為必填")
    @Min(value = 0, message = "啟用狀態設定錯誤")
    @Max(value = 1, message = "啟用狀態設定錯誤")
    private Integer status;

}
