package com.watergun.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("Pending_Amount_Log")
@AllArgsConstructor
@NoArgsConstructor
public class PendingAmountLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long pendingAmountLogId; // 日志ID，主键

    private Long merchantId; // 商家ID，关联merchants表

    private BigDecimal amount; // 变动的金额（正数表示增加，负数表示减少）

    private String currency; // 货币种类（如USD, EUR, CNY）

    private String description; // 变更描述，提供额外说明

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 记录创建时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 记录更新时间
}
