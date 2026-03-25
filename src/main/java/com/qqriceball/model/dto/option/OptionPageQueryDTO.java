package com.qqriceball.model.dto.option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "產品細節選項分頁查詢資料")
public class OptionPageQueryDTO {

    @Schema(description = "名稱")
    @Size(max = 20, message = "名稱最大長度為 20")
    private String title;

    @Schema(description = "選項類型(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度)")
    @NotNull(message = "選項類型為必填(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度)")
    @Min(value = 0, message = "選項類型設定錯誤(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度)")
    @Max(value = 4, message = "選項類型設定錯誤(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度)")
    private Integer optionType;

    @Schema(description = "上架狀態(0:下架, 1:上架, null:不指定狀態)")
    @Min(value = 0, message = "狀態設定錯誤，0:下架, 1:上架")
    @Max(value = 1, message = "狀態設定錯誤，0:下架, 1:上架")
    private Integer status;

    @Schema(description = "頁碼，預設為 1")
    @Min(value = 1, message = "頁碼不得小於 1")
    private Integer page = 1 ;

    @Schema(description = "每頁筆數，預設為 10")
    @Min(value = 1, message = "筆數不得小於 1")
    private Integer pageSize = 10;


}
