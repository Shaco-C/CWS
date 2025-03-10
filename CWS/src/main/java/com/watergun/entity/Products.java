package com.watergun.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.watergun.enums.ProductsStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    private Long categoryId;
    private String imageUrl;
    private Integer sales;

    @Enumerated(value = EnumType.STRING)
    private ProductsStatus status; //'pending', 'approved', 'rejected'

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
