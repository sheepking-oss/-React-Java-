package com.campus.secondhand.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.ReviewDTO;
import com.campus.secondhand.entity.Order;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.Review;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.ReviewMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.ReviewService;
import com.campus.secondhand.service.UserService;
import com.campus.secondhand.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReview(ReviewDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("只能评价自己购买的订单");
        }

        if (!"COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("只能评价已完成的订单");
        }

        Review existReview = this.getOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, dto.getOrderId()));

        if (existReview != null) {
            throw new RuntimeException("该订单已评价");
        }

        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setProductId(order.getProductId());
        review.setReviewerId(userId);
        review.setRevieweeId(order.getSellerId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        if (CollUtil.isNotEmpty(dto.getImageUrls())) {
            review.setImageUrls(String.join(",", dto.getImageUrls()));
        }
        review.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 0);

        this.save(review);

        int scoreChange = dto.getRating() >= 4 ? 2 : (dto.getRating() == 3 ? 0 : -2);
        userService.updateCreditScore(order.getSellerId(), scoreChange);
    }

    @Override
    public PageResult<Map<String, Object>> getMyReviews(Integer current, Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Page<Review> page = new Page<>(current, size);
        Page<Review> reviewPage = this.page(page, new LambdaQueryWrapper<Review>()
                .eq(Review::getReviewerId, userId)
                .orderByDesc(Review::getCreateTime));

        List<Map<String, Object>> records = new ArrayList<>();
        for (Review review : reviewPage.getRecords()) {
            records.add(buildReviewDetail(review));
        }

        return new PageResult<>(records, reviewPage.getTotal(), reviewPage.getSize(), reviewPage.getCurrent());
    }

    @Override
    public PageResult<Map<String, Object>> getUserReviews(Long userId, Integer current, Integer size) {
        Page<Review> page = new Page<>(current, size);
        Page<Review> reviewPage = this.page(page, new LambdaQueryWrapper<Review>()
                .eq(Review::getRevieweeId, userId)
                .orderByDesc(Review::getCreateTime));

        List<Map<String, Object>> records = new ArrayList<>();
        for (Review review : reviewPage.getRecords()) {
            records.add(buildReviewDetail(review));
        }

        return new PageResult<>(records, reviewPage.getTotal(), reviewPage.getSize(), reviewPage.getCurrent());
    }

    @Override
    public Map<String, Object> getReviewByOrderId(Long orderId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Review review = this.getOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, orderId));

        if (review == null) {
            return null;
        }

        return buildReviewDetail(review);
    }

    private Map<String, Object> buildReviewDetail(Review review) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", review.getId());
        result.put("orderId", review.getOrderId());
        result.put("productId", review.getProductId());
        result.put("reviewerId", review.getReviewerId());
        result.put("revieweeId", review.getRevieweeId());
        result.put("rating", review.getRating());
        result.put("content", review.getContent());
        result.put("isAnonymous", review.getIsAnonymous());
        result.put("createTime", review.getCreateTime());

        if (StrUtil.isNotBlank(review.getImageUrls())) {
            result.put("imageUrls", review.getImageUrls().split(","));
        }

        User reviewer = userMapper.selectById(review.getReviewerId());
        if (reviewer != null) {
            Map<String, Object> userMap = new HashMap<>();
            if (review.getIsAnonymous() == 1) {
                userMap.put("nickname", "匿名用户");
                userMap.put("avatar", null);
            } else {
                userMap.put("id", reviewer.getId());
                userMap.put("nickname", reviewer.getNickname());
                userMap.put("avatar", reviewer.getAvatar());
            }
            result.put("reviewer", userMap);
        }

        User reviewee = userMapper.selectById(review.getRevieweeId());
        if (reviewee != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", reviewee.getId());
            userMap.put("nickname", reviewee.getNickname());
            userMap.put("avatar", reviewee.getAvatar());
            result.put("reviewee", userMap);
        }

        Product product = productMapper.selectById(review.getProductId());
        if (product != null) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("title", product.getTitle());
            productMap.put("coverImage", product.getCoverImage());
            productMap.put("price", product.getPrice());
            result.put("product", productMap);
        }

        return result;
    }
}
