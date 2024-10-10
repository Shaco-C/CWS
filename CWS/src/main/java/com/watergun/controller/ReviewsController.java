package com.watergun.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.common.R;
import com.watergun.entity.Reviews;
import com.watergun.service.ReviewService;
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

    //用户发表评论
    @PostMapping
    public R<String> createReview(HttpServletRequest request, @RequestBody Reviews review) {
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return reviewService.createReview(token, review);
    }

    // 更新评论
    @PutMapping("/{reviewId}")
    public R<String> updateReview(@PathVariable Long reviewId, @RequestBody Reviews reviewDetails, HttpServletRequest request) {
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        return reviewService.updateReview(reviewId,token, reviewDetails);
    }

    //删除评论
    @DeleteMapping("/{reviewId}")
    public R<String> deleteReview(@PathVariable Long reviewId, HttpServletRequest request) {
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return reviewService.deleteReview(reviewId,token);
    }

    //用户可以查询自己发表过的所有评论
    @GetMapping("/user/page")
    public R<Page> userReviewPage(int page,int pageSize,HttpServletRequest request){
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return reviewService.getUserselfReviews(page,pageSize,token);
    }

}
