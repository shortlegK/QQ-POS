package com.qqriceball.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "員工編輯資料")
public class EmpEditDTO {

    @Schema(description = "員工編號")
    @NotNull(message = "請輸入員工編號")
    private Integer id;

    @Schema(description = "姓名")
    @Size(max = 20, message = "姓名最大長度為20")
    private String name;

    @Schema(description = "職位 (0: 管理職, 1: 一般員工)")
    @Min(value = 0, message = "職位設定錯誤")
    @Max(value = 1, message = "職位設定錯誤")
    private Integer role;

    @Schema(description = "到職日 yyyy-MM-dd ，選填")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;

}
