package com.watergun.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@TableName("order_items")
@AllArgsConstructor
@NoArgsConstructor
public class OrderItems {
    @TableId(type = IdType.ASSIGN_ID)
    private Long orderItemId;

    private Long orderId;
    private Long productId;
    private Integer quantity;
    private String returnStatus;
    private BigDecimal price;

}
