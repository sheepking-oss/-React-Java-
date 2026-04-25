package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.entity.Favorite;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.FavoriteMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.FavoriteService;
import com.campus.secondhand.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleFavorite(Long productId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        Favorite favorite = this.getOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));

        if (favorite != null) {
            this.removeById(favorite.getId());
            product.setFavoriteCount(Math.max(0, product.getFavoriteCount() - 1));
        } else {
            favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            this.save(favorite);
            product.setFavoriteCount(product.getFavoriteCount() + 1);
        }

        productMapper.updateById(product);
    }

    @Override
    public boolean isFavorite(Long productId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return false;
        }

        Favorite favorite = this.getOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));

        return favorite != null;
    }

    @Override
    public PageResult<Map<String, Object>> getMyFavorites(Integer current, Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Page<Favorite> page = new Page<>(current, size);
        Page<Favorite> favoritePage = this.page(page, new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime));

        List<Map<String, Object>> records = new ArrayList<>();
        for (Favorite favorite : favoritePage.getRecords()) {
            Product product = productMapper.selectById(favorite.getProductId());
            if (product != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", product.getId());
                item.put("userId", product.getUserId());
                item.put("title", product.getTitle());
                item.put("price", product.getPrice());
                item.put("coverImage", product.getCoverImage());
                item.put("status", product.getStatus());
                item.put("viewCount", product.getViewCount());
                item.put("favoriteCount", product.getFavoriteCount());
                item.put("createTime", product.getCreateTime());

                User user = userMapper.selectById(product.getUserId());
                if (user != null) {
                    Map<String, Object> seller = new HashMap<>();
                    seller.put("id", user.getId());
                    seller.put("nickname", user.getNickname());
                    seller.put("avatar", user.getAvatar());
                    item.put("seller", seller);
                }

                records.add(item);
            }
        }

        return new PageResult<>(records, favoritePage.getTotal(), favoritePage.getSize(), favoritePage.getCurrent());
    }
}
