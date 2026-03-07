package com.qqriceball.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "產品資料")
public class ProductVO {

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

}
