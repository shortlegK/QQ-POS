package com.qqriceball.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "菜單品項分頁查詢結果資料")
public class ProductPageQueryVO {

    @Schema(description = "編號")
    private Integer id;

    @Schema(description = "名稱")
    private String title;

    @Schema(description = "類型")
    private Integer productType;

    @Schema(description = "類型名稱")
    private String typeName;

    @Schema(description = "售價")
    private Integer price;

    @Schema(description = "狀態(0:停用 / 1:啟用)")
    private Integer status;

    // 關聯的加料選項
    private List<ProductOptionLinkNameVO> productOptionLinkNameVO;


}
