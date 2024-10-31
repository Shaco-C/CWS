package com.watergun.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user_addresses")
public class UserAddress {

    @TableId(type = IdType.ASSIGN_ID)
    private Long addressId; // 地址唯一ID

    private Long userId; // 用户ID，外键关联用户表

    private String recipientName; // 收件人姓名
    private String phoneCode; // 区号

    private String phoneNumber; // 联系电话

    private String country; // 国家

    private String state; // 州/省

    private String city; // 城市

    private String detailedAddress; // 详细地址

    private Boolean isDefault = false; // 是否默认地址，默认false

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt; // 更新时间

    public String getFullAddress(){
        return this.getCountry()+" "+this.getState()+" "+this.getCity()+" "
                +this.getDetailedAddress();
    }
}
