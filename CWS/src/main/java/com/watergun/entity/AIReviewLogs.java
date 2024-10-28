package com.watergun.entity;
import com.baomidou.mybatisplus.annotation.*;
import com.watergun.enums.AIReviewLogsResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Data
@TableName("ai_review_logs")
@AllArgsConstructor
@NoArgsConstructor
public class AIReviewLogs {
    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    private Long reviewId;
    @Enumerated(EnumType.STRING)
    private AIReviewLogsResult result;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
