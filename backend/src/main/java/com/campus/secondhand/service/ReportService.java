package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.ReportDTO;
import com.campus.secondhand.entity.Report;

import java.util.Map;

public interface ReportService extends IService<Report> {

    void createReport(ReportDTO dto);

    PageResult<Map<String, Object>> getMyReports(String status, Integer current, Integer size);

    Map<String, Object> getReportDetail(Long id);
}
