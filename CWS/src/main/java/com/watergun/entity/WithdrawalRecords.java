package com.watergun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("withdrawal_records")
public class WithdrawalRecords {

    @TableId(type = IdType.ASSIGN_ID)  // 使用雪花算法生成ID
    private Long withdrawalId;

    private Long merchantId;
    private BigDecimal amount;
    private String status;
    private String transactionId;
    private Long bankAccountId;
    private String currency;

//    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime requestTime;

//    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime completionTime;

    private String failureReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
