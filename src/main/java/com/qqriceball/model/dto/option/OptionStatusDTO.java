package com.qqriceball.model.dto.option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新選項狀態資料")
public class OptionStatusDTO {

    @Schema(description = "狀態(0:停用, 1:啟用)")
    @NotNull(message = "請輸入狀態(0:停用, 1:啟用)")
    @Min(value = 0, message = "狀態設定錯誤(0:停用, 1:啟用)")
    @Max(value = 1, message = "狀態設定錯誤(0:停用, 1:啟用)")
    private Integer status;
}
