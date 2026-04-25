package com.campus.secondhand.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.ProductDTO;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.ProductImage;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.ProductImageMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.ProductService;
import com.campus.secondhand.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private UserMapper userMapper;

    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "数码产品", "书籍教材", "生活用品", "服饰鞋包",
            "运动器材", "乐器配件", "美妆护肤", "其他"
    );

    @Override
    public PageResult<Map<String, Object>> getProductList(String keyword, String category, Integer status, Long userId, Integer current, Integer size) {
        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Product::getTitle, keyword)
                    .or().like(Product::getDescription, keyword)
                    .or().like(Product::getTags, keyword));
        }
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Product::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(Product::getUserId, userId);
        }

        wrapper.eq(Product::getAuditStatus, 1)
                .orderByDesc(Product::getCreateTime);

        Page<Product> productPage = this.page(page, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Product product : productPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", product.getId());
            item.put("userId", product.getUserId());
            item.put("title", product.getTitle());
            item.put("description", product.getDescription());
            item.put("price", product.getPrice());
            item.put("originalPrice", product.getOriginalPrice());
            item.put("category", product.getCategory());
            item.put("coverImage", product.getCoverImage());
            item.put("status", product.getStatus());
            item.put("viewCount", product.getViewCount());
            item.put("favoriteCount", product.getFavoriteCount());
            item.put("location", product.getLocation());
            item.put("tags", product.getTags());
            item.put("condition", product.getCondition());
            item.put("isNegotiable", product.getIsNegotiable());
            item.put("createTime", product.getCreateTime());

            User user = userMapper.selectById(product.getUserId());
            if (user != null) {
                Map<String, Object> seller = new HashMap<>();
                seller.put("id", user.getId());
                seller.put("nickname", user.getNickname());
                seller.put("avatar", user.getAvatar());
                seller.put("creditScore", user.getCreditScore());
                item.put("seller", seller);
            }

            records.add(item);
        }

        return new PageResult<>(records, productPage.getTotal(), productPage.getSize(), productPage.getCurrent());
    }

    @Override
    public Map<String, Object> getProductDetail(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        product.setViewCount(product.getViewCount() + 1);
        this.updateById(product);

        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        result.put("userId", product.getUserId());
        result.put("title", product.getTitle());
        result.put("description", product.getDescription());
        result.put("price", product.getPrice());
        result.put("originalPrice", product.getOriginalPrice());
        result.put("category", product.getCategory());
        result.put("coverImage", product.getCoverImage());
        result.put("status", product.getStatus());
        result.put("viewCount", product.getViewCount());
        result.put("favoriteCount", product.getFavoriteCount());
        result.put("location", product.getLocation());
        result.put("tags", product.getTags());
        result.put("condition", product.getCondition());
        result.put("isNegotiable", product.getIsNegotiable());
        result.put("auditStatus", product.getAuditStatus());
        result.put("createTime", product.getCreateTime());

        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, id)
                        .orderByAsc(ProductImage::getSortOrder)
        );
        List<String> imageUrls = new ArrayList<>();
        for (ProductImage image : images) {
            imageUrls.add(image.getImageUrl());
        }
        result.put("images", imageUrls);

        User user = userMapper.selectById(product.getUserId());
        if (user != null) {
            Map<String, Object> seller = new HashMap<>();
            seller.put("id", user.getId());
            seller.put("nickname", user.getNickname());
            seller.put("avatar", user.getAvatar());
            seller.put("creditScore", user.getCreditScore());
            seller.put("school", user.getSchool());
            seller.put("tradeCount", user.getTradeCount());
            seller.put("successCount", user.getSuccessCount());
            result.put("seller", seller);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishProduct(ProductDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Product product = new Product();
        product.setUserId(userId);
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setCategory(dto.getCategory());
        product.setCoverImage(dto.getCoverImage());
        product.setLocation(dto.getLocation());
        product.setTags(dto.getTags());
        product.setCondition(dto.getCondition());
        product.setIsNegotiable(dto.getIsNegotiable());
        product.setStatus(1);
        product.setViewCount(0);
        product.setFavoriteCount(0);
        product.setAuditStatus(0);

        this.save(product);

        saveProductImages(product.getId(), dto.getImages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Product product = this.getById(dto.getId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此商品");
        }

        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setCategory(dto.getCategory());
        product.setCoverImage(dto.getCoverImage());
        product.setLocation(dto.getLocation());
        product.setTags(dto.getTags());
        product.setCondition(dto.getCondition());
        product.setIsNegotiable(dto.getIsNegotiable());
        product.setAuditStatus(0);

        this.updateById(product);

        productImageMapper.delete(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, dto.getId()));

        saveProductImages(dto.getId(), dto.getImages());
    }

    @Override
    public void deleteProduct(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Product product = this.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此商品");
        }

        this.removeById(id);
    }

    @Override
    public void auditProduct(Long id, Integer auditStatus, String reason) {
        Product product = this.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        product.setAuditStatus(auditStatus);
        product.setAuditReason(reason);
        this.updateById(product);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Product product = this.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此商品状态");
        }

        product.setStatus(status);
        this.updateById(product);
    }

    @Override
    public List<String> getCategories() {
        return DEFAULT_CATEGORIES;
    }

    private void saveProductImages(Long productId, List<String> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        for (int i = 0; i < images.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(images.get(i));
            image.setSortOrder(i);
            productImageMapper.insert(image);
        }
    }
}
