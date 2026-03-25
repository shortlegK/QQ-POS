package com.qqriceball.model.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "產品編輯資料")
public class ProductEditDTO {

    @Schema(description = "產品 ID")
    @NotNull(message = "請輸入產品 ID")
    private Integer id;

    @Schema(description = "名稱")
    @NotBlank(message = "請輸入名稱，最大長度為 20")
    @Size(max = 20, message = "名稱最大長度為 20")
    private String title;

    @Schema(description = "產品類型(0:葷食, 1:素食, 2:飲料)")
    @NotNull(message = "請輸入產品類型(0:葷食, 1:素食, 2:飲料)")
    @Min(value = 0, message = "產品類型設定錯誤(0:葷食, 1:素食, 2:飲料)")
    @Max(value = 2, message = "產品類型設定錯誤(0:葷食, 1:素食, 2:飲料)")
    private Integer productType;

    @Schema(description = "售價")
    @NotNull(message = "請輸入售價，不得小於 1")
    @Min(value = 1, message = "售價不得小於 1")
    private Integer price;

    @Schema(description = "上架狀態(0:下架, 1:上架)")
    @NotNull(message = "請輸入上架狀態(0:下架, 1:上架)")
    @Min(value = 0, message = "上架狀態設定錯誤(0:下架, 1:上架)")
    @Max(value = 1, message = "上架狀態設定錯誤(0:下架, 1:上架)")
    private Integer status;

}
