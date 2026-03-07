package com.qqriceball.model.dto.option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "依類型查詢產品細節選項資料")
public class OptionActiveQueryDTO implements Serializable {

    @Schema(description = "選項類型(0: 米飯種類, 1: 飯量, 2: 辣度, 3: 加料種類 ,4:飲品溫度)")
    @NotNull
    @Min(value = 0, message = "選項類型設定錯誤")
    @Max(value = 4, message = "選項類型設定錯誤")
    private Integer optionType;

}
