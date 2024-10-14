package com.watergun.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("Return_Requests") // 指定表名
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRequest {

    @TableId(type = IdType.ASSIGN_ID) // 使用雪花算法生成主键
    private Long returnRequestId;

    private Long orderId; // 订单ID

    private Long productId; // 商品ID

    private Long userId; // 用户ID

    private String returnReason; // 退货原因

    private String status; // 退货申请状态 ('pending', 'approved', 'rejected')

    private LocalDateTime createdAt; // 申请创建时间

    private LocalDateTime updatedAt; // 更新时间
}
