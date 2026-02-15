package com.snackchain.snack_platform_backend.common.service;

import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 文件存储服务
 *
 * What: 本地文件存储（可后续迁移至 OSS）
 * Why: 商品图片、门店图片需要上传功能，当前只能手填 URL
 * Why good: 统一存储路径管理 + 文件类型/大小校验 + UUID 防重名
 */
@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:}")
    private String baseUrl;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    public String upload(MultipartFile file) {
        // 校验文件
        validateFile(file);

        try {
            // 确保上传目录存在
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            // 按日期子目录组织
            String datePath = java.time.LocalDate.now().toString().replace("-", "/");
            Path targetDir = uploadPath.resolve(datePath);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // 保存文件
            Path targetFile = targetDir.resolve(newFilename);
            file.transferTo(targetFile.toFile());

            // 返回访问 URL
            String relativePath = datePath + "/" + newFilename;
            String url = (baseUrl.isEmpty() ? "/uploads/" : baseUrl + "/") + relativePath;
            log.info("文件上传成功: {} -> {}", originalFilename, url);
            return url;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.ERROR, "文件上传失败");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.ERROR, "请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.ERROR, "文件大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ResultCode.ERROR, "只支持 JPG、PNG、GIF、WebP 格式的图片");
        }
    }
}
