package com.qqriceball.model.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "產品細節選項分頁查詢資料")
public class OrderPageQueryDTO implements Serializable {

    @Schema(description = "訂單編號")
    private String orderNo;

    @Schema(description = "訂單狀態(0: 製作中, 1: 待領取, 2: 已領取, 3: 已取消, null: 不指定狀態)")
    @Min(value = 0, message = "狀態設定錯誤，0: 製作中, 1: 待領取, 2: 已領取, 3: 已取消")
    @Max(value = 3, message = "狀態設定錯誤，0: 製作中, 1: 待領取, 2: 已領取, 3: 已取消")
    private Integer status;

    @Schema(description = "起始日期 yyyy-MM-dd")
    @NotNull(message = "起始日期為必填")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "結束日期 yyyy-MM-dd")
    @NotNull(message = "結束日期為必填")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "頁碼")
    @NotNull(message = "頁碼為必填")
    @Min(value = 1, message = "頁碼不得小於 1")
    private Integer page = 1 ;

    @Schema(description = "每頁筆數")
    @NotNull(message = "每頁筆數為必填")
    @Min(value = 1, message = "筆數不得小於 1")
    private Integer pageSize = 10;
}
