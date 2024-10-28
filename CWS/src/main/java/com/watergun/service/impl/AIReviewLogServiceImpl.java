package com.watergun.service.impl;

import com.watergun.common.R;
import com.watergun.entity.AIReviewLogs;
import com.watergun.entity.Reviews;
import com.watergun.enums.AIReviewLogsResult;
import com.watergun.enums.ReviewsStatus;
import com.watergun.mapper.AIReviewLogsMapper;
import com.watergun.service.AIReviewLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AIReviewLogServiceImpl extends ServiceImpl<AIReviewLogsMapper, AIReviewLogs> implements AIReviewLogService {


    private final OllamaChatModel ollamaChatModel;
    private final ReviewService reviewService;

    // 只有一个构造函数，Spring 会自动进行注入
    public AIReviewLogServiceImpl(OllamaChatModel ollamaChatModel, ReviewService reviewService) {
        this.ollamaChatModel = ollamaChatModel;
        this.reviewService = reviewService;
    }

    @Override
    public String chatToAI(String msg) {
        log.info("=====================chatToAI方法========================");
        ChatResponse chatResponse=ollamaChatModel.call(new Prompt(msg, OllamaOptions.create()
                .withModel("llama3.1:8b")//使用哪个大模型
                .withTemperature(0.4F)));//温度，温度值越高，准确率下降，温度值越低，准确率上升
        return chatResponse.getResult().getOutput().getContent();
    }

    //审核评论，approved就是通过，rejected就是不通过
    //在审核评论之后创建log,以及更新review评论
    @Override
    @Async
    public R<String> reviewIsOk(Long reviewId) {
        log.info("=====================reviewIsOk方法========================");
        Reviews msg = reviewService.getById(reviewId);

        String key=msg.getComment()+"If this comment does not offend anyone and is suitable for discussion by everyone. Then just reply with a simple word 'YES', otherwise just reply with 'NO'.";
        msg.setComment(key);
        ChatResponse chatResponse=ollamaChatModel.call(new Prompt(msg.getComment(), OllamaOptions.create()
                .withModel("llama3.1:8b")
                .withTemperature(0.4F)));
        String response = chatResponse.getResult().getOutput().getContent();
        AIReviewLogs aiReviewLogs = new AIReviewLogs();
        aiReviewLogs.setReviewId(msg.getReviewId());
        if (response.toUpperCase().contains("YES")){
            aiReviewLogs.setResult(AIReviewLogsResult.APPROVED);
            msg.setStatus(ReviewsStatus.APPROVED);
        }else{
            aiReviewLogs.setResult(AIReviewLogsResult.REJECTED);
            msg.setStatus(ReviewsStatus.REJECTED);
        }
        reviewService.updateById(msg);
        boolean result = this.save(aiReviewLogs);
        if (!result){
            return R.error("审核失败");
        }
        return R.success("审核成功");
    }
}
