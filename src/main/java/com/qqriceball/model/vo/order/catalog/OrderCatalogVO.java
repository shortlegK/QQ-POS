package com.qqriceball.model.vo.order.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "訂單商品目錄")
public class OrderCatalogVO {

    @Schema(description = "可訂購的商品列表")
    private List<OrderableProductVO> products;

    @Schema(description = "各商品類型對應的細節選項設定列表")
    private List<ProductOptionConfigVO> optionConfigs;
}
