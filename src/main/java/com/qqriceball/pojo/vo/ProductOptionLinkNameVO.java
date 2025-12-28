package com.qqriceball.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "加料選項建立/編輯資料")
public class ProductOptionLinkNameVO {

    private Integer id;

    @Schema(description = "名稱")
    private String title;

    @Schema(description = "類型 (0: 米飯種類, 1: 飯量, 2: 辣度, 3: 加料種類)")
    private Integer optionType;


}
