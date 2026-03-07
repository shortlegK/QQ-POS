package com.qqriceball.model.dto.emp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "員工登入資料")
public class EmpLoginDTO implements Serializable {

    @Schema(description = "帳號")
    @NotBlank(message = "請輸入帳號")
    @Size(min = 6, max = 20, message = "帳號長度為 6~20")
    private String username;

    @Schema(description = "密碼")
    @NotBlank(message = "請輸入密碼")
    private String password;

}
