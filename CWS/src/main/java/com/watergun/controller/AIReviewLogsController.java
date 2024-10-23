package com.watergun.controller;

import com.watergun.common.R;
import com.watergun.service.AIReviewLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai-review-logs")
public class AIReviewLogsController {


    private final AIReviewLogService aiReviewLogService;

    public AIReviewLogsController(AIReviewLogService aiReviewLogService) {
        this.aiReviewLogService = aiReviewLogService;
    }

    @RequestMapping("/aiChat")
    public R<Object> chatWithAI(@RequestParam(value = "msg")String msg){
        Object resp = aiReviewLogService.chatToAI(msg);
        return R.success(resp);
    }

    //AI审核
    @PostMapping("/aiCheckReview/{reviewId}")
    public R<String> aiCheckReview(@PathVariable("reviewId") Long reviewId){
        return aiReviewLogService.reviewIsOk(reviewId);
    }
}