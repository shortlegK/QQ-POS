package com.qqriceball.model.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "產品類型資料")
public class ProductTypeVO {

    @Schema(description = "產品類型 (0:葷食, 1:素食, 2:飲料)")
    private Integer productType;

    @Schema(description = "產品類型名稱")
    private String productTypeName;

}
