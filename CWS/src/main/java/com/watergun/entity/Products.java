package com.watergun.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
@AllArgsConstructor
@NoArgsConstructor
public class Products {
    @TableId(type = IdType.ASSIGN_ID)
    private Long productId;

    private Boolean isActive;
    private Long merchantId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer categoryId;
    private String imageUrl;
    private Integer sales;
    private String status; //'pending', 'approved', 'rejected'

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
