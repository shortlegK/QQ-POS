package com.qqriceball.model.entity.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "訂單資料")

public class Order {

    @Schema(description = "訂單 ID")
    private Integer id;

    @Schema(description = "訂單編號(yyyyMMdd+流水號)")
    private String orderNo;

    @Schema(description = "預計取餐時間")
    private LocalDateTime pickupTime;

    @Schema(description = "訂單總金額")
    private Integer total;

    @Schema(description = "訂單狀態(0: 製作中, 1: 待領取, 2: 已領取, 3:已取消)")
    private Integer status;

    @Schema(description = "訂單備註")
    private String notes;

    @Schema(description = "建立人員 ID")
    private Integer createId;

    @Schema(description = "建立時間")
    private LocalDateTime createTime;

    @Schema(description = "更新人員 ID")
    private Integer updateId;

    @Schema(description = "更新時間")
    private LocalDateTime updateTime;

}
