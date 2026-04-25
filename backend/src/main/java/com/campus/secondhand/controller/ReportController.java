package com.campus.secondhand.controller;

import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ReportDTO;
import com.campus.secondhand.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/create")
    public Result<Void> create(@Validated @RequestBody ReportDTO dto) {
        reportService.createReport(dto);
        return Result.success();
    }

    @GetMapping("/myReports")
    public Result<PageResult<Map<String, Object>>> myReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Map<String, Object>> result = reportService.getMyReports(status, current, size);
        return Result.success(result);
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> result = reportService.getReportDetail(id);
        return Result.success(result);
    }
}
