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


    private final ReviewService reviewService;

    public ReviewsController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    //用户发表评论
    @PostMapping
    public R<Reviews> createReview(HttpServletRequest request, @RequestBody Reviews review) {
        // 从请求头中获取 JWT
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return reviewService.createReview(token, review);
    }

    // 更新评论
    @PutMapping("/{reviewId}")
    public R<Reviews> updateReview(@PathVariable Long reviewId, @RequestBody Reviews reviewDetails, HttpServletRequest request) {
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        return reviewService.updateReview(reviewId, token, reviewDetails);
    }

    //删除评论
    @DeleteMapping("/{reviewId}")
    public R<String> deleteReview(@PathVariable Long reviewId, HttpServletRequest request) {
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return reviewService.deleteReview(reviewId, token);
    }

    //用户可以查询自己发表过的所有评论
    @GetMapping("/user/page")
    public R<Page> userReviewPage(int page, int pageSize, HttpServletRequest request) {
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return reviewService.getUserselfReviews(page, pageSize, token);
    }

    //获得产品所有通过审核的评论(通过productId查询)
    @GetMapping("/getReviewsByProductId/{productId}")
    public R<Page> getReviewsByProductId(@RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value = "pageSize", defaultValue = "1") int pageSize,
                                         @PathVariable Long productId){
        return reviewService.getApprovedReviewsByProductId( productId,page, pageSize);
    }

    //--------------------管理员方法-----------------
    //展示所有评论，按照status进行分类，如果前端没传默认查询所有的评论  (对管理员)
    @GetMapping("/admin/getReviewsPage")
    public R<Page> adminGetReviewsPage(@RequestParam(value = "page", defaultValue = "1") int page,
                                       @RequestParam(value = "pageSize", defaultValue = "1") int pageSize,
                                       String status) {

        return reviewService.adminGetReviewsPage(page, pageSize, status);
    }

    // 管理员审核评论（通过或拒绝）
    @PutMapping("/admin/reviewStatus/{reviewId}")
    public R<String> reviewStatus(@PathVariable Long reviewId, @RequestParam String status, HttpServletRequest request) {
        // 从请求头中获取 JWT token
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return reviewService.reviewStatus(reviewId, status, token);
    }
}