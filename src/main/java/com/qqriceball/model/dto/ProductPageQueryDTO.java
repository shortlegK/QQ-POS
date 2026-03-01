package com.qqriceball.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "菜單品項分頁查詢資料")
public class ProductPageQueryDTO implements Serializable {

    @Schema(description = "名稱")
    private String title;

    @Schema(description = "產品類型")
    @Min(value = 0, message = "產品類型設定錯誤")
    @Max(value = 2, message = "產品類型設定錯誤")
    private Integer productType;

    @Schema(description = "狀態")
    @Min(value = 0, message = "狀態設定錯誤")
    @Max(value = 1, message = "狀態設定錯誤")
    private Integer status;

    @Schema(description = "頁碼")
    @NotNull(message = "頁碼為必填")
    @Min(value = 1, message = "頁碼不得為 0")
    private Integer page = 1 ;

    @Schema(description = "每頁筆數")
    @NotNull(message = "每頁筆數為必填")
    @Min(value = 1, message = "筆數不得為 0")
    private Integer pageSize = 10;


}
