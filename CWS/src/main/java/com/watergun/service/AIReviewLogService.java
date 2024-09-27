package com.watergun.service;

import com.watergun.entity.AIReviewLogs;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.entity.Reviews;

public interface AIReviewLogService extends IService<AIReviewLogs> {
    // 你可以在这里添加自定义的方法
    String chatToAI(String msg);

    void reviewIsOk(Reviews msg);
}
