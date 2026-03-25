package com.qqriceball.model.dto.emp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "員工分頁查詢資料")
public class EmpPageQueryDTO {

    @Schema(description = "員工姓名")
    @Size(max = 20, message = "姓名最大長度為20")
    private String name;

    @Schema(description = "狀態(0:停用, 1:啟用, null:不指定狀態)")
    @Min(value = 0, message = "狀態設定錯誤(0:停用, 1:啟用, null:不指定狀態)")
    @Max(value = 1, message = "狀態設定錯誤(0:停用, 1:啟用, null:不指定狀態)")
    private Integer status;

    @Schema(description = "頁碼，預設為 1")
    @Min(value = 1, message = "頁碼不得為 0")
    private Integer page = 1;

    @Schema(description = "每頁筆數，預設為 10")
    @Min(value = 1, message = "筆數不得為 0")
    private Integer pageSize = 10;


}
