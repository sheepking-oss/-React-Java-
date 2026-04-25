package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.ProductDTO;
import com.campus.secondhand.entity.Product;

import java.util.List;
import java.util.Map;

public interface ProductService extends IService<Product> {

    PageResult<Map<String, Object>> getProductList(String keyword, String category, Integer status, Long userId, Integer current, Integer size);

    Map<String, Object> getProductDetail(Long id);

    void publishProduct(ProductDTO dto);

    void updateProduct(ProductDTO dto);

    void deleteProduct(Long id);

    void auditProduct(Long id, Integer auditStatus, String reason);

    void updateStatus(Long id, Integer status);

    List<String> getCategories();
}
