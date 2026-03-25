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

    @Schema(description = "選項 ID")
    private Integer id;

    @Schema(description = "名稱")
    private String title;

    @Schema(description = "選項類型(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度)")
    private Integer optionType;

    @Schema(description = "是否為預設值 (0:否, 1:是)")
    private Integer isDefault;

    @Schema(description = "售價")
    private Integer price;

    @Schema(description = "上架狀態(0:下架, 1:上架)")
    private Integer status;

}
