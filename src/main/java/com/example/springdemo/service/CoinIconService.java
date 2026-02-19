package com.example.springdemo.service;

import com.example.springdemo.biz.CoinIconBiz;
import com.example.springdemo.common.error.BusinessException;
import com.example.springdemo.common.error.ErrorCode;
import com.example.springdemo.common.error.IntegrationException;
import com.example.springdemo.common.logging.LogContext;
import com.example.springdemo.config.CoinIconProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class CoinIconService implements CoinIconBiz {
    private static final Logger log = LoggerFactory.getLogger(CoinIconService.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp", "svg");

    private final CoinIconProperties coinIconProperties;

    public CoinIconService(CoinIconProperties coinIconProperties) {
        this.coinIconProperties = coinIconProperties;
    }

    @Override
    public String upload(MultipartFile file) {
        validateFile(file);

        String extension = resolveExtension(file);
        Path uploadDir = resolveUploadDir();
        String filename = "coin-icon-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().replace("-", "");
        if (StringUtils.hasText(extension)) {
            filename = filename + "." + extension;
        }

        Path targetPath = uploadDir.resolve(filename).normalize();
        if (!targetPath.startsWith(uploadDir)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "invalid upload path");
        }

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IntegrationException(ErrorCode.INTEGRATION_IO_ERROR, "failed to save icon file", e);
        }

        String publicPath = normalizePublicPath(coinIconProperties.getPublicPath());
        String iconUrl = publicPath + "/" + filename;
        log.info(
            "business traceId={} userId={} operation=upload_coin_icon details=filename:{},size:{} result=SUCCESS",
            LogContext.traceId(),
            LogContext.currentUserId(),
            filename,
            file.getSize()
        );
        return iconUrl;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "icon file is required");
        }

        Long maxSizeKb = coinIconProperties.getMaxSizeKb();
        if (maxSizeKb == null || maxSizeKb <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "coin.icon.max-size-kb must be a positive number");
        }
        long maxSizeBytes = maxSizeKb * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_TOO_LARGE, "icon file size exceeds limit: " + maxSizeKb + "KB");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "only image files are supported");
        }

        String extension = resolveExtension(file);
        if (StringUtils.hasText(extension) && !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(
                ErrorCode.INVALID_ARGUMENT,
                "unsupported image format, allowed: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }
    }

    private String resolveExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (StringUtils.hasText(filename)) {
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > -1 && dotIndex < filename.length() - 1) {
                return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
            }
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/pjpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            default -> "";
        };
    }

    private Path resolveUploadDir() {
        String uploadDir = coinIconProperties.getUploadDir();
        if (!StringUtils.hasText(uploadDir)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "coin.icon.upload-dir is required");
        }
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String normalizePublicPath(String publicPath) {
        if (!StringUtils.hasText(publicPath)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "coin.icon.public-path is required");
        }
        String normalized = publicPath.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
