package com.qqriceball.model.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "訂單編輯資料")
public class OrderEditDTO {

    @Schema(description = "訂單編號(yyyyMMdd+流水號)")
    @NotBlank(message = "請輸入訂單編號")
    private String orderNo;

    @Schema(description = "預計取餐時間 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickupTime;

    @Schema(description = "訂單商品列表")
    @NotEmpty(message = "請輸入訂單商品")
    private List<@Valid OrderItemDTO> items;
}
