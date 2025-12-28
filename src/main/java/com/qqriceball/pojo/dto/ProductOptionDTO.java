package com.qqriceball.pojo.dto;

import com.qqriceball.pojo.entity.ProductOptionLink;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "加料選項建立/編輯資料")
public class ProductOptionDTO {

    private Integer id;

    @Schema(description = "名稱")
    @NotBlank(message = "名稱為必填")
    private String title;

    @Schema(description = "類型 (0: 米飯種類, 1: 飯量, 2: 辣度, 3: 加料種類)")
    @NotNull(message = "類型為必填")
    @Min(value = 0, message = "類型設定錯誤")
    @Max(value = 3, message = "類型設定錯誤")
    private Integer optionType;

    @Schema(description = "售價")
    @NotNull(message = "售價為必填")
    @Min(value = 1, message = "售價不得小於 1")
    private Integer price;

    @Schema(description = "狀態(0:停用 / 1:啟用)")
    @NotNull(message = "狀態為必填")
    @Min(value = 0, message = "狀態設定錯誤")
    @Max(value = 1, message = "狀態設定錯誤")
    private Integer status;

    @Schema(description = "加料選項")
    private List<ProductOptionLink> productOptionLinks = new ArrayList<>();
}
