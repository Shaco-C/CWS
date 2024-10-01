package com.watergun.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("favorites")
@AllArgsConstructor
@NoArgsConstructor
public class Favorites {
    @TableId(type = IdType.ASSIGN_ID)
    private Long favoriteId;

    private Long userId;
    private Long productId;

    private LocalDateTime createdAt;
}
