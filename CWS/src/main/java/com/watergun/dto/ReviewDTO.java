package com.watergun.dto;

import com.watergun.entity.Reviews;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long reviewId;
    private Long productId;
    private Long userId;
    private Integer rating;
    private String comment;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private String avatarUrl;

    public ReviewDTO(Reviews review, String username, String avatarUrl) {
        this.reviewId = review.getReviewId();
        this.productId = review.getProductId();
        this.userId = review.getUserId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.status = review.getStatus();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
}
