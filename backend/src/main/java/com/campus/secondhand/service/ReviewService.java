package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.ReviewDTO;
import com.campus.secondhand.entity.Review;

import java.util.Map;

public interface ReviewService extends IService<Review> {

    void createReview(ReviewDTO dto);

    PageResult<Map<String, Object>> getMyReviews(Integer current, Integer size);

    PageResult<Map<String, Object>> getUserReviews(Long userId, Integer current, Integer size);

    Map<String, Object> getReviewByOrderId(Long orderId);
}
