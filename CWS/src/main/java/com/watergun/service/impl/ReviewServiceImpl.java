package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.dto.ReviewDTO;
import com.watergun.entity.Reviews;
import com.watergun.entity.Users;
import com.watergun.mapper.ReviewsMapper;
import com.watergun.service.ReviewService;
import com.watergun.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReviewServiceImpl extends ServiceImpl<ReviewsMapper, Reviews> implements ReviewService {

    @Autowired
    private UserService userService;

    //展示产品通过审核的评论
    @Override
    public List<ReviewDTO> getApprovedReviewsByProductId(Long productId) {

        // 查询所有通过审核的评论
        LambdaQueryWrapper<Reviews> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reviews::getProductId, productId)
                .eq(Reviews::getStatus, "approved")
                .orderByDesc(Reviews::getUpdatedAt);
        List<Reviews> reviewsList = list(queryWrapper);
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

        // 使用流将 Reviews 转换为 ReviewDTO
        List<ReviewDTO> reviewDTOList = reviewsList.stream().map(review -> {
            Users user = userMap.get(review.getUserId());
            return new ReviewDTO(review, user.getUsername(), user.getAvatarUrl());
        }).toList();

        return reviewDTOList;
    }

}
