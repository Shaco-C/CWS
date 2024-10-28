package com.watergun.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("wallet_balance_log")
public class WalletBalanceLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long walletBalanceLogId;  // 日志ID

    private Long merchantId;          // 商家ID

    private Long orderId;             // 订单ID

    private BigDecimal amountChange;  // 变动金额

    private BigDecimal newBalance;    // 变动后余额

    private String currency;          // 货币种类

    private String description;       // 变更描述

    private LocalDateTime createdAt;  // 日志创建时间


}
