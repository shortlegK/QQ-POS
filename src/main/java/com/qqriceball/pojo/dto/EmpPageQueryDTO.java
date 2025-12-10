package com.qqriceball.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class EmpPageQueryDTO implements Serializable {

    @Schema(description = "員工姓名")
    private String name;

    @Schema(description = "頁碼")
    @NotNull(message = "頁碼為必填")
    private Integer page;

    @Schema(description = "每頁筆數")
    @NotNull(message = "每頁筆數為必填")
    private Integer pageSize;


}
