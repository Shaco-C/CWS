package com.watergun.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("ai_review_logs")
@AllArgsConstructor
@NoArgsConstructor
public class AIReviewLogs {
    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    private Long reviewId;
    private String result;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
