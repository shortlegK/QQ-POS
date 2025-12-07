package com.qqriceball.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "登入傳入的資料")
public class EmpLoginDTO implements Serializable {

    @Schema(description = "帳號")
    private String username;

    @Schema(description = "密碼")
    private String password;

}
