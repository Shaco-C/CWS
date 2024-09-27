package com.watergun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("categories")
@AllArgsConstructor
@NoArgsConstructor
public class Categories {
    @TableId(type = IdType.ASSIGN_ID)
    private Long categoryId;

    private String name;
    private Integer parentId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
