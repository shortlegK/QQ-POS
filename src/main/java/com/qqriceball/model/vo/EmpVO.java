package com.qqriceball.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "員工資料")
public class EmpVO {

    @Schema(description = "員工 ID")
    private Integer id;

    @Schema(description = "帳號")
    private String username;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "職位 (0:管理, 1:一般)")
    private Integer role;

    @Schema(description = "狀態(0:停用, 1:啟用)")
    private Integer status;

    @Schema(description = "入職日期")
    private LocalDate entryDate;

}
