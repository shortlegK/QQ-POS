package com.qqriceball.model.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "產品詳細資料")
public class Product {

    @Schema(description = "編號")
    private Integer id;

    @Schema(description = "名稱")
    private String title;

    @Schema(description = "產品類型 (0: 葷食, 1: 素食, 2: 飲料)")
    private Integer productType;

    @Schema(description = "售價")
    private Integer price;

    @Schema(description = "上架狀態(0:下架 / 1:上架)")
    private Integer status;

    @Schema(description = "建立人員 id")
    private Integer createId;

    @Schema(description = "建立時間")
    private LocalDateTime createTime;

    @Schema(description = "更新人員 id")
    private Integer updateId;

    @Schema(description = "更新時間")
    private LocalDateTime updateTime;

}
