package com.qqriceball.model.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "訂單狀態資料")
public class OrderStatusDTO {

    @Schema(description = "訂單狀態(0:製作中, 1:待領取, 2:已領取, 3:已取消)")
    @NotNull(message = "請輸入訂單狀態(0:製作中, 1:待領取, 2:已領取, 3:已取消)")
    @Min(value = 0, message = "訂單狀態設定錯誤(0:製作中, 1:待領取, 2:已領取, 3:已取消)")
    @Max(value = 3, message = "訂單狀態設定錯誤(0:製作中, 1:待領取, 2:已領取, 3:已取消)")
    private Integer status;

}