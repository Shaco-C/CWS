package com.watergun.controller;

import com.watergun.common.R;
import com.watergun.entity.AIReviewLogs;
import com.watergun.service.AIReviewLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai-review-logs")
public class AIReviewLogsController {

    @Autowired
    private AIReviewLogService aiReviewLogService;

    @RequestMapping("/ai")
    public R<Object> chatWithAI(@RequestParam(value = "msg")String msg){
        Object resp = aiReviewLogService.chatToAI(msg);
        return R.success(resp);
    }
}