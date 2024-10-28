package com.watergun.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("cart_items")
@AllArgsConstructor
@NoArgsConstructor
public class CartItems {
    @TableId(type = IdType.ASSIGN_ID)
    private Long cartItemId;

    private Long cartId;
    private Long productId;
    private Integer quantity;
}
