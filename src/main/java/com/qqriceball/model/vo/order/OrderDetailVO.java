package com.qqriceball.model.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "訂單詳細資料")
public class OrderDetailVO {

    @Schema(description = "訂單編號")
    private String orderNo;

    @Schema(description = "預計取餐時間")
    private LocalDateTime pickupTime;

    @Schema(description = "訂單總金額")
    private Integer total;

    @Schema(description = "訂單狀態(0: 製作中, 1: 待領取, 2: 已領取, 3: 已取消)")
    private Integer status;

    @Schema(description = "訂單成立時間")
    private LocalDateTime createTime;

    @Schema(description = "最後更新人員名稱")
    private String updateName;

    @Schema(description = "訂單商品明細")
    private List<OrderItemVO> items;

}
