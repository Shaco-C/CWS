package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.common.R;
import com.watergun.dto.ReviewDTO;
import com.watergun.entity.Reviews;
import com.watergun.entity.Users;
import com.watergun.mapper.ReviewsMapper;
import com.watergun.service.ReviewService;
import com.watergun.service.UserService;
import com.watergun.utils.JwtUtil;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReviewServiceImpl extends ServiceImpl<ReviewsMapper, Reviews> implements ReviewService {

    private final UserService userService;

    private final JwtUtil jwtUtil;

    public ReviewServiceImpl(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    //展示产品通过审核的评论
    @Override
    public R<Page> getApprovedReviewsByProductId(Long productId, int page, int pageSize) {

        // 创建分页对象
        Page<Reviews> pageInfo = new Page<>(page, pageSize);

        // 查询所有通过审核的评论，并分页
        LambdaQueryWrapper<Reviews> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reviews::getProductId, productId)
                .eq(Reviews::getStatus, "approved")
                .orderByDesc(Reviews::getUpdatedAt);

        // 使用分页查询
        Page<Reviews> reviewPage = this.page(pageInfo, queryWrapper);
        List<Reviews> reviewsList = reviewPage.getRecords();
        log.info("reviewsList: {}", reviewsList);

        // 获取所有需要查询的用户 ID
        List<Long> userIds = reviewsList.stream().map(Reviews::getUserId).distinct().toList();
        log.info("userIds: {}", userIds);

        // 调用批量查询用户信息的方法
        List<Users> usersList = userService.getUsersByIds(userIds);
        log.info("usersList: {}", usersList);

        // 将用户信息转换成 Map 以便快速查找
        Map<Long, Users> userMap = usersList.stream()
                .collect(Collectors.toMap(Users::getUserId, user -> user));
        log.info("userMap: {}", userMap);

        // 使用流将 Reviews 转换为 ReviewDTO，并保持分页结构
        List<ReviewDTO> reviewDTOList = reviewsList.stream().map(review -> {
            Users user = userMap.get(review.getUserId());
            return new ReviewDTO(review, user.getUsername(), user.getAvatarUrl());
        }).toList();

        // 将转换后的 DTO 列表设置回分页对象中
        Page<ReviewDTO> reviewDTOPage = new Page<>(pageInfo.getCurrent(), pageInfo.getSize(), pageInfo.getTotal());
        reviewDTOPage.setRecords(reviewDTOList);

        return R.success(reviewDTOPage);
    }


    //用户发表评论
    @Override
    public R<Reviews> createReview(String token, Reviews review) {
        log.info("调用用户发表评论请求");
        log.info("token: {}", token);
        log.info("review: {}", review);

        Long userId = jwtUtil.extractUserId(token);
        log.info("userId: {}", userId);

        review.setStatus("pending");
        review.setUserId(userId);
        log.info("review: {}", review);

        boolean result = this.save(review);
        if (!result){
            return R.error("评论提交失败");
        }
        return R.success(review);
    }

    //更新评论
    @Override
    public R<Reviews> updateReview(Long reviewId, String token, Reviews reviewDetails) {
        log.info("调用用户更新评论请求");
        log.info("reviewId: {}", reviewId);
        log.info("token: {}", token);
        log.info("reviewDetails: {}", reviewDetails);

        Long userId = jwtUtil.extractUserId(token);
        log.info("userId: {}", userId);
        //根据ID获取评论详情。如果评论不存在，则返回错误信息
        Reviews review = this.getById(reviewId);
        log.info("review: {}", review);
        if (review == null) {
            return R.error("评论不存在");
        }

        //检查评论是否属于当前用户
        if (!review.getUserId().equals(userId)) {
            return R.error("评论不属于当前用户");
        }

        //更新评论内容
        review.setComment(reviewDetails.getComment());
        review.setRating(reviewDetails.getRating());
        review.setStatus("pending");
        boolean result = this.updateById(review);
        if (!result){
            return R.error("评论更新失败");
        }
        return R.success(review);
    }

    //删除评论
    @Override
    public R<String> deleteReview(Long reviewId, String token) {
        log.info("调用用户删除评论请求");
        log.info("reviewId: {}", reviewId);
        log.info("token: {}", token);

        Long userId = jwtUtil.extractUserId(token);
        log.info("userId: {}", userId);
        String userRole = jwtUtil.extractRole(token);
        log.info("userRole: {}", userRole);

        //根据ID获取评论详情。如果评论不存在，则返回错误信息
        Reviews review = this.getById(reviewId);
        log.info("review: {}", review);
        if (review == null) {
            return R.error("评论不存在");
        }
        //检查评论是否属于当前用户或管理员
        if (!review.getUserId().equals(userId) && !userRole.equals("admin")) {
            return R.error("你无权删除这条评论");
        }
        this.removeById(reviewId);
        return R.success("评论删除成功");
    }

    @Override
    public R<Page> getUserselfReviews(int page, int pageSize, String token) {
        log.info("调用用户获取自己的评论请求");
        log.info("page: {}", page);
        log.info("pageSize: {}", pageSize);
        log.info("token: {}", token);

        Long userId = jwtUtil.extractUserId(token);
        log.info("userId: {}", userId);

        LambdaQueryWrapper<Reviews> reviewsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        reviewsLambdaQueryWrapper.eq(Reviews::getUserId, userId)
                .orderByDesc(Reviews::getCreatedAt);

        //根据用户ID分页查询评论
        Page pageInfo = new Page<>(page,pageSize);
        this.page(pageInfo, reviewsLambdaQueryWrapper);

        return R.success(pageInfo);
    }
    //---------------------管理员方法----------------------
    //展示所有评论，按照status进行分类，如果前端没传默认查询所有的评论  (对管理员)
    @Override
    public R<Page> adminGetReviewsPage(int page, int pageSize, String status){
        log.info("page: {}, pageSize: {}, status: {}", page, pageSize, status);
        Page pageInfo = new Page(page,pageSize);
        log.info("查看评论");
        LambdaQueryWrapper<Reviews> reviewsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        reviewsLambdaQueryWrapper.like(StringUtils.isNotEmpty(status),Reviews::getStatus,status)
                .orderByDesc(Reviews::getUpdatedAt); //按照时间排序
        this.page(pageInfo,reviewsLambdaQueryWrapper);
        return R.success(pageInfo);
    }

    // 管理员审核评论（通过或拒绝）
    @Override
    public R<String> reviewStatus(Long reviewId, String status, String token){
        log.info("token: {}", token);
        // 提取 JWT 中的用户角色
        String userRole = jwtUtil.extractRole(token);
        log.info("userRole: {}", userRole);

        // 检查用户是否是管理员
        if (!"admin".equals(userRole)) {
            return R.error("你无权进行审核操作");
        }

        // 根据评论ID获取评论详情
        Reviews review = this.getById(reviewId);
        if (review == null) {
            return R.error("评论不存在");
        }

        // 更新评论状态
        review.setStatus(status);
        this.updateById(review);

        return R.success("评论状态更新为: " + status);
    }
}
