package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.entity.Favorite;

import java.util.Map;

public interface FavoriteService extends IService<Favorite> {

    void toggleFavorite(Long productId);

    boolean isFavorite(Long productId);

    PageResult<Map<String, Object>> getMyFavorites(Integer current, Integer size);
}
