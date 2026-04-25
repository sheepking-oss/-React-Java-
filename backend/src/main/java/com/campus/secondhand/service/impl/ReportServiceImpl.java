package com.campus.secondhand.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.ReportDTO;
import com.campus.secondhand.entity.Product;
import com.campus.secondhand.entity.Report;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.ReportMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.ReportService;
import com.campus.secondhand.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public void createReport(ReportDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        if (userId.equals(dto.getReportedUserId())) {
            throw new RuntimeException("不能举报自己");
        }

        User reportedUser = userMapper.selectById(dto.getReportedUserId());
        if (reportedUser == null) {
            throw new RuntimeException("被举报用户不存在");
        }

        if (dto.getProductId() != null) {
            Product product = productMapper.selectById(dto.getProductId());
            if (product == null) {
                throw new RuntimeException("举报的商品不存在");
            }
        }

        Report report = new Report();
        report.setReporterId(userId);
        report.setReportedUserId(dto.getReportedUserId());
        report.setProductId(dto.getProductId());
        report.setType(dto.getType());
        report.setReason(dto.getReason());
        if (CollUtil.isNotEmpty(dto.getImageUrls())) {
            report.setImageUrls(String.join(",", dto.getImageUrls()));
        }
        report.setStatus("PENDING");

        this.save(report);
    }

    @Override
    public PageResult<Map<String, Object>> getMyReports(String status, Integer current, Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Page<Report> page = new Page<>(current, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<Report>()
                .eq(Report::getReporterId, userId)
                .orderByDesc(Report::getCreateTime);

        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Report::getStatus, status);
        }

        Page<Report> reportPage = this.page(page, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Report report : reportPage.getRecords()) {
            records.add(buildReportDetail(report));
        }

        return new PageResult<>(records, reportPage.getTotal(), reportPage.getSize(), reportPage.getCurrent());
    }

    @Override
    public Map<String, Object> getReportDetail(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Report report = this.getById(id);
        if (report == null) {
            throw new RuntimeException("举报不存在");
        }

        if (!report.getReporterId().equals(userId)) {
            throw new RuntimeException("无权查看此举报");
        }

        return buildReportDetail(report);
    }

    private Map<String, Object> buildReportDetail(Report report) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", report.getId());
        result.put("reporterId", report.getReporterId());
        result.put("reportedUserId", report.getReportedUserId());
        result.put("productId", report.getProductId());
        result.put("type", report.getType());
        result.put("reason", report.getReason());
        result.put("status", report.getStatus());
        result.put("handleResult", report.getHandleResult());
        result.put("handleTime", report.getHandleTime());
        result.put("createTime", report.getCreateTime());

        if (StrUtil.isNotBlank(report.getImageUrls())) {
            result.put("imageUrls", report.getImageUrls().split(","));
        }

        User reportedUser = userMapper.selectById(report.getReportedUserId());
        if (reportedUser != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", reportedUser.getId());
            userMap.put("nickname", reportedUser.getNickname());
            userMap.put("avatar", reportedUser.getAvatar());
            result.put("reportedUser", userMap);
        }

        if (report.getProductId() != null) {
            Product product = productMapper.selectById(report.getProductId());
            if (product != null) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", product.getId());
                productMap.put("title", product.getTitle());
                productMap.put("coverImage", product.getCoverImage());
                result.put("product", productMap);
            }
        }

        return result;
    }
}
