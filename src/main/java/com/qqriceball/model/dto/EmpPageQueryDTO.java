package com.qqriceball.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "員工分頁查詢資料")
public class EmpPageQueryDTO implements Serializable {

    @Schema(description = "員工姓名")
    private String name;

    @Schema(description = "頁碼")
    @NotNull(message = "請輸入頁碼")
    @Min(value = 1, message = "頁碼不得為 0")
    private Integer page = 1;

    @Schema(description = "每頁筆數")
    @NotNull(message = "請輸入每頁筆數")
    @Min(value = 1, message = "筆數不得為 0")
    private Integer pageSize = 10;


}
