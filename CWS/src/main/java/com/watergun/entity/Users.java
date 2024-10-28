package com.watergun.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.watergun.enums.UserRoles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Data
@TableName("users")
@AllArgsConstructor
@NoArgsConstructor
public class Users {
    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;

    private String username;
    private String email;
    private String password;
    private String phoneNumber;

    @Enumerated(EnumType.STRING) // 保存为字符串
    private UserRoles role; // ADMIN,MERCHANT,USER

    private String language;
    private String avatarUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
