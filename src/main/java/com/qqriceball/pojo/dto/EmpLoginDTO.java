package com.qqriceball.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "員工登入資料")
public class EmpLoginDTO implements Serializable {

    @Schema(description = "帳號")
    @NotBlank(message = "帳號為必填")
    private String username;

    @Schema(description = "密碼")
    @NotBlank(message = "密碼為必填")
    private String password;

}
