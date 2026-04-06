package com.qqriceball.model.dto.revenue;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "營收統計查詢資料")
public class RevenueDTO {

    @Schema(description = "統計期間類型 (0:當日, 1:當周, 2:當月)")
    @NotNull(message = "統計期間類型不能為空，0:當日, 1:當周, 2:當月")
    @Min(value = 0, message = "統計期間類型設定異常，0:當日, 1:當周, 2:當月")
    @Max(value = 2, message = "統計期間類型設定異常，0:當日, 1:當周, 2:當月")
    private int periodType;

}
