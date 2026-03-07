package com.qqriceball.model.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "產品分頁查詢資料")
public class ProductPageQueryDTO implements Serializable {

    @Schema(description = "名稱")
    @Size(max = 20, message = "名稱最大長度為 20")
    private String title;

    @Schema(description = "產品類型 (0: 葷食, 1: 素食, 2: 飲料)")
    @Min(value = 0, message = "產品類型設定錯誤")
    @Max(value = 2, message = "產品類型設定錯誤")
    private Integer productType;

    @Schema(description = "上架狀態(0: 下架, 1: 上架, null: 不指定狀態)")
    @Min(value = 0, message = "狀態設定錯誤，0: 下架, 1: 上架")
    @Max(value = 1, message = "狀態設定錯誤，0: 下架, 1: 上架")
    private Integer status;

    @Schema(description = "頁碼")
    @NotNull(message = "頁碼為必填")
    @Min(value = 1, message = "頁碼不得小於 1")
    private Integer page = 1 ;

    @Schema(description = "每頁筆數")
    @NotNull(message = "每頁筆數為必填")
    @Min(value = 1, message = "筆數不得小於 1")
    private Integer pageSize = 10;


}
