package com.campus.secondhand.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "数码产品", "书籍教材", "生活用品", "服饰鞋包",
            "运动器材", "乐器配件", "美妆护肤", "其他"
    );

    private static final String CONTENT_HASH_KEY_PREFIX = "product:content:hash:";

    private static final long CONTENT_HASH_EXPIRE_SECONDS = 300;

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

        String contentHash = generateContentHash(userId, dto);
        checkDuplicateByContentHash(userId, contentHash, "publish");

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

    private String generateContentHash(Long userId, ProductDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append(userId).append(":");
        sb.append(dto.getTitle()).append(":");
        sb.append(dto.getDescription()).append(":");
        sb.append(dto.getPrice()).append(":");
        sb.append(dto.getCategory()).append(":");
        sb.append(dto.getCondition()).append(":");
        sb.append(dto.getIsNegotiable()).append(":");

        if (CollUtil.isNotEmpty(dto.getImages())) {
            sb.append(String.join(",", dto.getImages()));
        }

        String hash = DigestUtil.md5Hex(sb.toString());
        logger.info("生成商品内容哈希, userId: {}, hash: {}", userId, hash);
        return hash;
    }

    private void checkDuplicateByContentHash(Long userId, String contentHash, String action) {
        if (redisTemplate == null) {
            logger.warn("Redis 未配置，跳过内容哈希校验");
            return;
        }

        String key = CONTENT_HASH_KEY_PREFIX + userId + ":" + contentHash;

        try {
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(exists)) {
                logger.warn("检测到重复提交，userId: {}, action: {}, hash: {}", userId, action, contentHash);
                throw new RuntimeException("请勿重复提交，请稍后再试");
            }

            redisTemplate.opsForValue().set(key, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    CONTENT_HASH_EXPIRE_SECONDS, TimeUnit.SECONDS);
            logger.info("保存内容哈希，key: {}, expireSeconds: {}", key, CONTENT_HASH_EXPIRE_SECONDS);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("内容哈希校验异常: {}", e.getMessage(), e);
        }
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
