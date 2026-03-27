package com.qqriceball.model.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = """
                      訂單分頁查詢資料
                      查詢規則：
                      - orderNo、startDate、endDate 皆為選填
                      - 若三者皆未填寫，預設查詢當日資料
                      - 若僅填寫 orderNo，不限日期範圍進行查詢
                      - 若填寫 startDate 和 endDate，則查詢該日期範圍內的資料
                      """)
public class OrderPageQueryDTO {

    @Schema(description = "訂單編號")
    private String orderNo;

    @Schema(description = "訂單狀態(0:製作中, 1:待領取, 2:已領取, 3:已取消, null:不指定狀態)")
    @Min(value = 0, message = "狀態設定錯誤(0:製作中, 1:待領取, 2:已領取, 3:已取消)")
    @Max(value = 3, message = "狀態設定錯誤(0:製作中, 1:待領取, 2:已領取, 3:已取消)")
    private Integer status;

    @Schema(description = "起始日期 yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "結束日期 yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "頁碼，預設為 1")
    @Min(value = 1, message = "頁碼不得小於 1")
    private Integer page = 1 ;

    @Schema(description = "每頁筆數，預設為 10")
    @Min(value = 1, message = "筆數不得小於 1")
    private Integer pageSize = 10;
}
