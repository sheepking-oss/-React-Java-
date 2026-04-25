package com.campus.secondhand.controller;

import com.campus.secondhand.annotation.Idempotent;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ProductDTO;
import com.campus.secondhand.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Map<String, Object>> result = productService.getProductList(keyword, category, status, userId, current, size);
        return Result.success(result);
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> product = productService.getProductDetail(id);
        return Result.success(product);
    }

    @GetMapping("/category")
    public Result<List<String>> getCategories() {
        List<String> categories = productService.getCategories();
        return Result.success(categories);
    }

    @Idempotent(action = "product:publish", expireSeconds = 120)
    @PostMapping("/publish")
    public Result<Void> publish(@Validated @RequestBody ProductDTO dto) {
        productService.publishProduct(dto);
        return Result.success();
    }

    @Idempotent(action = "product:update", expireSeconds = 120)
    @PutMapping("/update")
    public Result<Void> update(@Validated @RequestBody ProductDTO dto) {
        productService.updateProduct(dto);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }
}
