package com.watergun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.entity.Reviews;
import com.watergun.service.AIReviewLogService;
import com.watergun.service.ReviewService;
import com.watergun.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/reviews")
@Slf4j
public class ReviewsController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AIReviewLogService aiReviewLogService;

    @Autowired
    private JwtUtil jwtUtil;

    //用户发表评论
    @PostMapping
    public R<String> createReview(HttpServletRequest request, @RequestBody Reviews review) {
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        log.info("token: {}", token);
        // 解析 JWT 获取用户 ID
        Long userId = jwtUtil.extractUserId(token);
        log.info("userId: {}", userId);
        review.setUserId(userId);
        review.setStatus("pending");
        log.info("review: {}", review);
        reviewService.save(review);
        // 调用 AI 异步评审
        aiReviewLogService.reviewIsOk(review);
        return R.success("评论已提交，待审核");
    }

    // 更新评论  通过评论的id
    @PutMapping("/{id}")
    public R<String> updateReview(@PathVariable Long id, @RequestBody Reviews reviewDetails, HttpServletRequest request) {
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        log.info("token: {}", token);
        // 提取 JWT 中的用户 ID
        Long userId = jwtUtil.extractUserId(token);
        log.info("userId: {}", userId);
        // 根据评论ID获取评论详情
        Reviews review = reviewService.getById(id);
        log.info("review: {}", review);
        // 如果评论不存在
        if (review == null) {
            return R.error("评论不存在");
        }

        // 验证该评论是否属于当前用户
        if (!review.getUserId().equals(userId)) {
            return R.error("你无权更新该评论");
        }

        // 更新评论内容
        review.setComment(reviewDetails.getComment());

        // 更新评分（如果有评分功能）
        if (reviewDetails.getRating() != null) {
            review.setRating(reviewDetails.getRating());
        }
        review.setStatus("pending");
        // 保存更新后的评论
        reviewService.updateById(review);

        //更新之后需要重新进行审核
        aiReviewLogService.reviewIsOk(review);
        return R.success("提交更新评论请求成功");
    }

    //删除评论
    @DeleteMapping("/{id}")
    public R<String> deleteReview(@PathVariable Long id, HttpServletRequest request) {
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        log.info("token: {}", token);
        // 提取 JWT 中的用户 ID
        Long userId = jwtUtil.extractUserId(token);
        log.info("userId: {}", userId);
        // 提取 JWT 中的用户角色
        String userRole = jwtUtil.extractRole(token);
        log.info("userRole: {}", userRole);
        // 根据评论ID获取评论详情
        Reviews review = reviewService.getById(id);
        log.info("review: {}", review);
        // 如果评论不存在
        if (review == null) {
            return R.error("评论不存在");
        }

        // 验证该评论是否属于当前用户或者用户是否是管理员
        if (!review.getUserId().equals(userId)&&!userRole.equals("admin")) {
            return R.error("你无权删除该评论");
        }

        // 删除评论
        reviewService.removeById(id);

        return R.success("评论删除成功");
    }



    //用户可以查询自己发表过的所有评论
    @GetMapping("/user/page")
    public R<Page> userReviewPage(int page,int pageSize,HttpServletRequest request){
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        log.info("token: {}", token);

        // 提取 JWT 中的用户 ID
        Long userId = jwtUtil.extractUserId(token);

        log.info("userId: {}的用户查询自己发送的所有评论", userId);

        // 根据用户ID分页查询评论
        LambdaQueryWrapper<Reviews> reviewsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        reviewsLambdaQueryWrapper.eq(Reviews::getUserId, userId)
                .orderByDesc(Reviews::getCreatedAt);

        Page pageInfo = new Page(page,pageSize);
        reviewService.page(pageInfo,reviewsLambdaQueryWrapper);

        return R.success(pageInfo);
    }

}
