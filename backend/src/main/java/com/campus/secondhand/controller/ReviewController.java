package com.campus.secondhand.controller;

import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ReviewDTO;
import com.campus.secondhand.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/create")
    public Result<Void> create(@Validated @RequestBody ReviewDTO dto) {
        reviewService.createReview(dto);
        return Result.success();
    }

    @GetMapping("/myReviews")
    public Result<PageResult<Map<String, Object>>> myReviews(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Map<String, Object>> result = reviewService.getMyReviews(current, size);
        return Result.success(result);
    }

    @GetMapping("/user/{userId}")
    public Result<PageResult<Map<String, Object>>> userReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Map<String, Object>> result = reviewService.getUserReviews(userId, current, size);
        return Result.success(result);
    }

    @GetMapping("/byOrder/{orderId}")
    public Result<Map<String, Object>> getByOrderId(@PathVariable Long orderId) {
        Map<String, Object> result = reviewService.getReviewByOrderId(orderId);
        return Result.success(result);
    }
}
