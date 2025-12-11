package com.qqriceball.common.result;

import com.qqriceball.pojo.vo.EmpPageQueryVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult implements Serializable {

    @Schema(description = "總紀錄數")
    private Long total;

    @Schema(description = "當前頁數")
    private Integer page;

    @Schema(description = "每頁筆數")
    private Integer pageSize;

    @Schema(description = "分頁查詢資料")
    private List<EmpPageQueryVO> records;
}
