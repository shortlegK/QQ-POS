package com.qqriceball.model.vo.order.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "細節選項群組")
public class OptionGroupVO {

    @Schema(description = "選項類型(0:米飯種類, 1:飯量, 2:辣度, 3:加料種類, 4:飲品溫度, 5:去除配料)")
    private Integer optionType;

    @Schema(description = "是否必填(true:必填, false:選填")
    private Boolean required;

    @Schema(description = "是否單選(true:單選, false:可複選)")
    private Boolean singleSelect;

    @Schema(description = "可選擇的選項列表")
    private List<OrderableOptionVO> options;


}
