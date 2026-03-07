package com.qqriceball.model.dto.option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
@Schema(description = "產品細節選項編輯資料")
public class OptionEditDTO {

    @Schema(description = "編號")
    @NotNull(message = "請輸入編號")
    private Integer id;

    @Schema(description = "名稱")
    private String title;

    @Schema(description = "選項類型 (0: 米飯種類, 1: 飯量, 2: 辣度, 3: 加料種類, 4:飲品溫度)")
    @Min(value = 0, message = "選項類型設定錯誤")
    @Max(value = 4, message = "選項類型設定錯誤")
    private Integer optionType;

    @Schema(description = "是否為預設值 (0:否 / 1:是)")
    @Min(value = 0, message = "預設設定錯誤")
    @Max(value = 1, message = "預設設定錯誤")
    private Integer isDefault;

    @Schema(description = "售價")
    @Min(value = 0, message = "售價不得小於 1")
    private Integer price;

    @Schema(description = "上架狀態(0:下架 / 1:上架)")
    @Min(value = 0, message = "上架狀態設定錯誤")
    @Max(value = 1, message = "上架狀態設定錯誤")
    private Integer status;

}
