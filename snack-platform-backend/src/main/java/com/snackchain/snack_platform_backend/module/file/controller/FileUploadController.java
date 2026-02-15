package com.snackchain.snack_platform_backend.module.file.controller;

import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.common.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "文件上传")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * 上传图片
     */
    @Operation(summary = "上传图片文件")
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.upload(file);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return Result.success(result);
    }
}
