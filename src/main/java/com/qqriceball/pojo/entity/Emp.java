package com.qqriceball.pojo.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "員工資料")
public class Emp {

    @Schema(description = "員工編號")
    private Integer id;

    @Schema(description = "帳號")
    private String username;

    @Schema(description = "密碼")
    private String password;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "職位")
    private Integer role;

    @Schema(description = "狀態(停用/啟用)")
    private Integer status;

    @Schema(description = "入職日期")
    private LocalDate entryDate;

    @Schema(description = "建立時間")
    private LocalDateTime createTime;

    @Schema(description = "更新時間")
    private LocalDateTime updateTime;

}
