package com.watergun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.entity.Reviews;
import com.watergun.service.AIReviewLogService;
import com.watergun.service.ReviewService;
import com.watergun.utils.JwtUtil;
import io.micrometer.common.util.StringUtils;
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


    //展示所有评论，按照status进行分类，如果前端没传默认查询所有的评论  (对管理员)
    @GetMapping("/admin/page")
    public R<Page> page(int page, int pageSize, String status){
        log.info("page: {}, pageSize: {}, status: {}", page, pageSize, status);
        Page pageInfo = new Page(page,pageSize);
        log.info("查看评论");
        LambdaQueryWrapper<Reviews> reviewsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        reviewsLambdaQueryWrapper.like(StringUtils.isNotEmpty(status),Reviews::getStatus,status)
                .orderByDesc(Reviews::getUpdatedAt); //按照时间排序
        reviewService.page(pageInfo,reviewsLambdaQueryWrapper);
        return R.success(pageInfo);
    }

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

    // 审核评论（通过或拒绝）
    @PutMapping("/admin/reviewStatus/{id}")
    public R<String> reviewStatus(@PathVariable Long id, @RequestParam String status, HttpServletRequest request) {
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        log.info("token: {}", token);
        // 提取 JWT 中的用户角色
        String userRole = jwtUtil.extractRole(token);
        log.info("userRole: {}", userRole);

        // 检查用户是否是管理员
        if (!"admin".equals(userRole)) {
            return R.error("你无权进行审核操作");
        }

        // 根据评论ID获取评论详情
        Reviews review = reviewService.getById(id);
        if (review == null) {
            return R.error("评论不存在");
        }

        // 更新评论状态
        review.setStatus(status);
        reviewService.updateById(review);

        return R.success("评论状态更新为: " + status);
    }


}
