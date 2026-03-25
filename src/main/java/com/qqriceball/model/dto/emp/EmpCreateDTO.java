package com.qqriceball.model.dto.emp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "員工新增資料")
public class EmpCreateDTO {


    @Schema(description = "帳號")
    @NotBlank(message = "請輸入帳號，長度為 6~20，只能由小寫字母與數字組成")
    @Size(min = 6, max = 20, message = "帳號長度為 6~20，只能由小寫字母與數字組成")
    @Pattern(
            regexp = "^(?=.*[a-z])[a-z!\\d]+$",
            message = "帳號只能由小寫字母與數字組成"
    )
    private String username;

    @Schema(description = "密碼")
    @NotBlank(message = "請輸入密碼，需包含至少一個大寫字母、一個小寫字母與一個數字，長度為 8~50")
    @Size(min = 8, max = 50, message = "密碼長度為 8~50，需包含至少一個大寫字母、一個小寫字母與一個數字")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+]+$",
            message = "密碼長度為 8~50，需包含至少一個大寫字母、一個小寫字母與一個數字"
    )
    private String password;

    @Schema(description = "姓名")
    @NotBlank(message = "請輸入姓名，最大長度為20")
    @Size(max = 20, message = "姓名最大長度為20")
    private String name;

    @Schema(description = "職位(0:管理職, 1:一般員工)")
    @NotNull(message = "請輸入職位(0:管理職, 1:一般員工)")
    @Min(value = 0, message = "職位設定錯誤(0:管理職, 1:一般員工)")
    @Max(value = 1, message = "職位設定錯誤(0:管理職, 1:一般員工)")
    private Integer role;

    @Schema(description = "到職日 yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;

}
