package com.heartape.whisper.controller;

import com.heartape.whisper.common.Result;
import com.heartape.whisper.config.UploadProperties;
import com.heartape.whisper.entity.result.UploadResult;
import com.heartape.whisper.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@RestController
public class FileController {

    private final UploadProperties uploadProperties;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @PostMapping("/upload/avatar")
    public Result<UploadResult> uploadAvatar(@RequestPart("file") MultipartFile file) {
        Path dirPath = Paths.get(uploadProperties.getAvatar().getDir()).toAbsolutePath();
        final String name = UUID.randomUUID().toString().replace("-", "");
        String fileName = storeFile(file, dirPath, name);
        final String filePath = uploadProperties.getAvatar().getPrefix() + "/" + fileName;
        final String url = uploadProperties.getServer() + filePath;
        final UploadResult uploadResult = new UploadResult(filePath, url);
        return Result.success(uploadResult);
    }

    @PostMapping("/upload/icon")
    public Result<UploadResult> uploadIcon(@RequestPart("file") MultipartFile file) {
        Path dirPath = Paths.get(uploadProperties.getIcon().getDir()).toAbsolutePath();
        final String name = UUID.randomUUID().toString().replace("-", "");
        String fileName = storeFile(file, dirPath, name);
        final String filePath = uploadProperties.getIcon().getPrefix() + "/" + fileName;
        final String url = uploadProperties.getServer() + filePath;
        final UploadResult uploadResult = new UploadResult(filePath, url);
        return Result.success(uploadResult);
    }

    private String storeFile(MultipartFile file, Path path, String fileName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("仅支持 jpg、png、webp 图片");
        }
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new BusinessException("创建目录失败:" + path);
        }
        String extension = getExtension(contentType);
        String fileNameAll = fileName + extension;
        Path newFilePath = path.resolve(fileNameAll);
        try {
            file.transferTo(newFilePath.toFile());
        } catch (IOException e) {
            try {
                Files.deleteIfExists(newFilePath);
            } catch (IOException ex) {
                throw new BusinessException("删除文件路径失败:" + newFilePath);
            }
            throw new BusinessException("上传文件失败,路径:" + newFilePath + ",原因:" + e.getMessage());
        }
        return fileNameAll;
    }

    private void deleteFile(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        final String accessPrefix = uploadProperties.getAvatar().getPrefix();
        if (!url.startsWith(accessPrefix + "/")) {
            return;
        }

        String oldFileName = url.substring((accessPrefix + "/").length());
        Path oldPath = Paths.get(uploadProperties.getAvatar().getDir()).toAbsolutePath().resolve(oldFileName);

        try {
            Files.deleteIfExists(oldPath);
        } catch (IOException ex) {
            throw new BusinessException("删除文件路径失败:" + oldPath);
        }
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".img";
        };
    }

}
