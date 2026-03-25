package com.qqriceball.model.dto.emp;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "員工更新密碼資料")
public class EmpUpdatePasswordDTO {

    @Schema(description = "舊密碼")
    @NotBlank(message = "請輸入舊密碼")
    private String oldPassword;

    @Schema(description = "新密碼")
    @NotBlank(message = "請輸入新密碼，需包含至少一個大寫字母、一個小寫字母與一個數字，長度為 8~50")
    @Size(min = 8, max = 50, message = "新密碼長度為 8~50，需包含至少一個大寫字母、一個小寫字母與一個數字")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+]+$",
            message = "新密碼長度為 8~50，需包含至少一個大寫字母、一個小寫字母與一個數字"
    )
    private String newPassword;
}
