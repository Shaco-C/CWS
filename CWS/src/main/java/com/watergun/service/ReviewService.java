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


    //-----------管理员方法-----------
    //展示所有评论，按照status进行分类，如果前端没传默认查询所有的评论  (对管理员)
    R<Page> adminGetReviewsPage(int page, int pageSize, String status);

    // 管理员审核评论（通过或拒绝）
    R<String> reviewStatus(Long reviewId,String status,String token);

}
