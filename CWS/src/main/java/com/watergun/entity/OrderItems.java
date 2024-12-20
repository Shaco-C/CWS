package com.watergun.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;

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

    private BigDecimal price;

}
