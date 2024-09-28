package com.watergun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.common.R;
import com.watergun.dto.ReviewDTO;
import com.watergun.entity.Reviews;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface ReviewService extends IService<Reviews> {
    //展示产品通过审核的评论
    List<ReviewDTO> getApprovedReviewsByProductId(Long productId);

}
