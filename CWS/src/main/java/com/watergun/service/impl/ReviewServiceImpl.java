package com.watergun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.entity.Reviews;
import com.watergun.mapper.ReviewsMapper;
import com.watergun.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewsMapper, Reviews> implements ReviewService {


    @Override
    public List<Reviews> getApprovedReviewsByProductId(Long productId) {
        LambdaQueryWrapper<Reviews> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reviews::getProductId, productId)
                .eq(Reviews::getStatus, "approved")
                .orderByDesc(Reviews::getUpdatedAt);;
        return list(queryWrapper);
    }
}
