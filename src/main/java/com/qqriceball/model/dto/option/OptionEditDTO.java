package com.qqriceball.model.dto.option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;


@Data
@Schema(description = "產品細節選項編輯資料")
public class OptionEditDTO {

    @Schema(description = "選項 ID")
    @NotNull(message = "請輸入選項 ID")
    private Integer id;

    @Schema(description = "名稱")
    @NotBlank(message = "請輸入名稱，最大長度為 20")
    @Size(max = 20, message = "名稱最大長度為 20")
    private String title;

    @Schema(description = "選項類型(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度, 5:去除配料)")
    @NotNull(message = "請輸入選項類型(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度, 5:去除配料)")
    @Min(value = 0, message = "選項類型設定錯誤(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度, 5:去除配料)")
    @Max(value = 5, message = "選項類型設定錯誤(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度, 5:去除配料)")
    private Integer optionType;

    @Schema(description = "是否為預設值(0:否, 1:是)")
    @NotNull(message = "請輸入是否為預設(0:否, 1:是)")
    @Min(value = 0, message = "預設設定錯誤(0:否, 1:是)")
    @Max(value = 1, message = "預設設定錯誤(0:否, 1:是)")
    private Integer isDefault;

    @Schema(description = "售價")
    @NotNull(message = "請輸入售價，不得小於 0")
    @Min(value = 0, message = "售價不得小於 1")
    private Integer price;

    @Schema(description = "上架狀態(0:下架, 1:上架)")
    @NotNull(message = "請輸入上架狀態(0:下架, 1:上架)")
    @Min(value = 0, message = "上架狀態設定錯誤(0:下架, 1:上架)")
    @Max(value = 1, message = "上架狀態設定錯誤(0:下架, 1:上架)")
    private Integer status;

}
