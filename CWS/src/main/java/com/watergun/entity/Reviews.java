package com.watergun.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("reviews")
@AllArgsConstructor
@NoArgsConstructor
public class Reviews {
    @TableId(type = IdType.ASSIGN_ID)
    private Long reviewId;

    private Long productId;
    private Long userId;
    private Integer rating;
    private String comment;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
