package com.watergun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.entity.Reviews;

import java.util.List;

public interface ReviewService extends IService<Reviews> {
    public List<Reviews> getApprovedReviewsByProductId(Long productId);
}
