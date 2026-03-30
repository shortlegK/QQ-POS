package com.qqriceball.model.vo.emp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "員工登入結果資料")
public class EmpLoginVO {

    @Schema(description = "員工 ID")
    private Integer id;

    @Schema(description = "帳號")
    private String username;

    @Schema(description = "職位(0:管理職, 1:一般員工)")
    private Integer role;

    @Schema(description = "姓名")
    private String name;

}