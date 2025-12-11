package com.qqriceball.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "員工登入結果資料")
public class EmpLoginVO implements Serializable {

    @Schema(description = "員工 id")
    private Integer id;

    @Schema(description = "帳號")
    private String username;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "token")
    private String token;

}