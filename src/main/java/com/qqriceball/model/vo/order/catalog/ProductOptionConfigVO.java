package com.qqriceball.model.vo.order.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品類型對應的細節選項設定")
public class ProductOptionConfigVO {

    @Schema(description = "商品類型(0:葷食, 1:素食, 2:飲品)")
    private Integer productType;

    @Schema(description = "商品類型名稱")
    private String productTypeName;

    @Schema(description = "細節選項群組列表")
    private List<OptionGroupVO> optionGroups;

}
