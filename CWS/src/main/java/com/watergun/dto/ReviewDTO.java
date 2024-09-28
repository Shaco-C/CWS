package com.watergun.dto;

import com.watergun.entity.Reviews;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO extends Reviews {
    private String username;
    private String avatarUrl;
    public ReviewDTO(Reviews review, String username, String avatarUrl) {
        this.setReviewId(review.getReviewId());
        this.setProductId(review.getProductId());
        this.setUserId(review.getUserId());
        this.setRating(review.getRating());
        this.setComment(review.getComment());
        this.setStatus(review.getStatus());
        this.setCreatedAt(review.getCreatedAt());
        this.setUpdatedAt(review.getUpdatedAt());
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    public ReviewDTO(Reviews review) {
        this.setReviewId(review.getReviewId());
        this.setProductId(review.getProductId());
        this.setUserId(review.getUserId());
        this.setRating(review.getRating());
        this.setComment(review.getComment());
        this.setStatus(review.getStatus());
        this.setCreatedAt(review.getCreatedAt());
        this.setUpdatedAt(review.getUpdatedAt());
    }
}
