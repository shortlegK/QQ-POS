package com.qqriceball.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "產品細節選項資料")
public class OptionVO {

    @Schema(description = "編號")
    private Integer id;

    @Schema(description = "名稱")
    private String title;

    @Schema(description = "選項類型")
    private Integer optionType;

    @Schema(description = "售價")
    private Integer price;

    @Schema(description = "狀態(0:停用 / 1:啟用)")
    private Integer status;

}
