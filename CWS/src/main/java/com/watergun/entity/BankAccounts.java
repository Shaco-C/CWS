package com.watergun.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("bank_accounts")
public class BankAccounts {

    @TableId(type = IdType.ASSIGN_ID)  // 使用雪花算法生成ID
    private Long bankAccountId;

    private Long userId;
    private String accountHolderName;
    private String bankName;
    private String accountNumber;
    private String iban;
    private String swiftCode;
    private String currency;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
