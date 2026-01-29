package com.qqriceball.model.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "產品及加料選項關聯表")

public class ProductOptionLink {
    @Schema(description = "產品id")
    private Integer productId;

    @Schema(description = "加料選項id")
    private Integer optionId;
}
