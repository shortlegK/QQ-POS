package com.qqriceball.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新員工狀態資料")
public class EmpStatusDTO {

    @Schema(description = "狀態 (0: 停用, 1: 啟用)")
    @NotNull(message = "狀態為必填")
    @Min(value = 0, message = "狀態設定錯誤")
    @Max(value = 1, message = "狀態設定錯誤")
    private Integer status;
}
