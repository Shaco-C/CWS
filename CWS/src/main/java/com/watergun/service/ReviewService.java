package com.watergun.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.dto.ReviewDTO;
import com.watergun.entity.Reviews;

import java.util.List;

public interface ReviewService extends IService<Reviews> {
    //展示产品通过审核的评论
    List<ReviewDTO> getApprovedReviewsByProductId(Long productId);

    // 创建评论
    R<String> createReview(String token, Reviews review);

    // 更新评论
    R<String> updateReview(Long reviewId, String token, Reviews reviewDetails);

    // 删除评论
    R<String> deleteReview(Long reviewId, String token);

    // 获取用户的评论（分页）
    R<Page> getUserselfReviews(int page, int pageSize, String token);


}
