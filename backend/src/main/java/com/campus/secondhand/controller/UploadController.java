package com.campus.secondhand.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.campus.secondhand.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${server.port:8080}")
    private String port;

    private static final String UPLOAD_PATH = "upload/";

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String fileName = IdUtil.simpleUUID() + suffix;

        File uploadDir = new File(UPLOAD_PATH);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        try {
            File destFile = new File(UPLOAD_PATH + fileName);
            file.transferTo(destFile);

            String url = "/upload/" + fileName;

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("filename", fileName);

            return Result.success(result);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/images")
    public Result<List<Map<String, String>>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("文件不能为空");
        }

        List<Map<String, String>> result = new ArrayList<>();

        File uploadDir = new File(UPLOAD_PATH);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = IdUtil.simpleUUID() + suffix;

            try {
                File destFile = new File(UPLOAD_PATH + fileName);
                file.transferTo(destFile);

                String url = "/upload/" + fileName;

                Map<String, String> item = new HashMap<>();
                item.put("url", url);
                item.put("filename", fileName);

                result.add(item);
            } catch (IOException e) {
                // 跳过失败的文件
            }
        }

        return Result.success(result);
    }
}
