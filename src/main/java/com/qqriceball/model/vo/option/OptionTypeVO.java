package com.qqriceball.model.vo.option;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "選項類型資料")
public class OptionTypeVO {

    @Schema(description = "選項類型(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度, 5:去除配料)")
    private Integer optionType;

    @Schema(description = "選項類型名稱")
    private String optionTypeName;

}
