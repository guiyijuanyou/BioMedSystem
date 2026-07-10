package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.FileAsset;
import com.cqutcm.biomed.mapper.FileAssetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class FileAssetService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "webp", "pdf",
            "mp4", "webm", "mov", "mp3", "txt", "csv",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "cdf", "mzml", "mzxml", "jdx", "dx"
    );
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "教学资料", "教学视频", "课程课件", "图片资料", "图谱文件",
            "培训材料", "评价佐证", "溯源附件"
    );
    private static final Set<String> DANGEROUS_CONTENT_TYPES = Set.of(
            "text/html", "image/svg+xml", "application/javascript", "text/javascript",
            "application/xhtml+xml"
    );

    private final FileAssetMapper mapper;
    private final Path uploadDir;
    private final long maxBytes;

    public FileAssetService(
            FileAssetMapper mapper,
            @Value("${app.upload-dir:data/uploads}") String uploadDir,
            @Value("${app.upload-max-bytes:268435456}") long maxBytes
    ) {
        this.mapper = mapper;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    public List<FileAsset> list() {
        return mapper.findAll();
    }

    public Optional<FileAsset> findById(String id) {
        return Optional.ofNullable(mapper.findById(id));
    }

    public FileAsset upload(MultipartFile upload, String category) {
        try {
            if (upload == null || upload.isEmpty()) {
                throw new IllegalArgumentException("请选择要上传的文件");
            }
            String originalName = upload.getOriginalFilename() == null || upload.getOriginalFilename().isBlank()
                    ? ""
                    : upload.getOriginalFilename();
            String fileName = validateMetadata(originalName, upload.getSize(), upload.getContentType(), category);
            validateMagicBytes(fileName, upload.getInputStream().readNBytes(16));
            Files.createDirectories(uploadDir);
            String id = UUID.randomUUID().toString();
            Path target = safeTarget(id, fileName);
            upload.transferTo(target);
            return saveFile(id, fileName, normalizeCategory(category), upload.getSize(), target);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("文件上传失败", ex);
        }
    }

    public FileAsset uploadBase64(Map<String, String> request) {
        try {
            byte[] content = Base64.getDecoder().decode(request.getOrDefault("contentBase64", ""));
            String fileName = validateMetadata(request.getOrDefault("fileName", ""), content.length,
                    request.get("contentType"), request.get("category"));
            validateMagicBytes(fileName, java.util.Arrays.copyOf(content, Math.min(content.length, 16)));
            Files.createDirectories(uploadDir);
            String id = UUID.randomUUID().toString();
            Path target = safeTarget(id, fileName);
            Files.write(target, content, StandardOpenOption.CREATE_NEW);
            return saveFile(id, fileName, normalizeCategory(request.get("category")), content.length, target);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("文件上传失败", ex);
        }
    }

    public Path requireStoredFile(FileAsset file) {
        if (file == null || file.getStoragePath() == null || file.getStoragePath().isBlank()) {
            throw new IllegalArgumentException("文件不存在");
        }
        Path stored = Path.of(file.getStoragePath()).toAbsolutePath().normalize();
        if (!stored.startsWith(uploadDir) || !Files.isRegularFile(stored)) {
            throw new IllegalArgumentException("文件不存在或存储路径无效");
        }
        return stored;
    }

    private FileAsset saveFile(String id, String fileName, String category, long size, Path target) {
        FileAsset file = new FileAsset();
        file.setId(id);
        file.setFileName(fileName);
        file.setCategory(category);
        file.setSizeBytes(size);
        file.setStoragePath(target.toString());
        file.setCreatedAt(LocalDateTime.now());
        try {
            mapper.insert(file);
            return file;
        } catch (RuntimeException ex) {
            try { Files.deleteIfExists(target); } catch (Exception ignored) {}
            throw ex;
        }
    }

    private String sanitizeFileName(String fileName) {
        String sanitized = fileName.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        if (sanitized.length() > 200) {
            throw new IllegalArgumentException("文件名不能超过 200 个字符");
        }
        return sanitized;
    }

    private String validateMetadata(String originalName, long size, String contentType, String category) {
        if (size <= 0) throw new IllegalArgumentException("文件内容不能为空");
        if (size > maxBytes) throw new IllegalArgumentException("文件大小超过系统限制");
        String fileName = sanitizeFileName(originalName == null ? "" : originalName);
        String extension = extension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持该文件类型: ." + extension);
        }
        String normalizedType = contentType == null ? "" : contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (DANGEROUS_CONTENT_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException("禁止上传可执行网页或脚本文件");
        }
        normalizeCategory(category);
        return fileName;
    }

    private String normalizeCategory(String category) {
        String normalized = category == null || category.isBlank() ? "教学资料" : category.trim();
        if (!ALLOWED_CATEGORIES.contains(normalized)) {
            throw new IllegalArgumentException("不支持的资料分类");
        }
        return normalized;
    }

    private Path safeTarget(String id, String fileName) {
        Path target = uploadDir.resolve(id + "-" + fileName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new IllegalArgumentException("无效的文件路径");
        }
        return target;
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) {
            throw new IllegalArgumentException("文件必须包含有效扩展名");
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private void validateMagicBytes(String fileName, byte[] bytes) {
        String extension = extension(fileName);
        boolean valid = switch (extension) {
            case "png" -> startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "jpg", "jpeg" -> startsWith(bytes, 0xFF, 0xD8, 0xFF);
            case "gif" -> startsWithAscii(bytes, "GIF87a") || startsWithAscii(bytes, "GIF89a");
            case "webp" -> startsWithAscii(bytes, "RIFF") && asciiAt(bytes, 8, "WEBP");
            case "pdf" -> startsWithAscii(bytes, "%PDF-");
            case "mp4", "mov" -> asciiAt(bytes, 4, "ftyp");
            case "webm" -> startsWith(bytes, 0x1A, 0x45, 0xDF, 0xA3);
            case "mp3" -> startsWithAscii(bytes, "ID3") || (bytes.length >= 2 && unsigned(bytes[0]) == 0xFF && (unsigned(bytes[1]) & 0xE0) == 0xE0);
            case "docx", "xlsx", "pptx" -> startsWith(bytes, 0x50, 0x4B, 0x03, 0x04);
            case "doc", "xls", "ppt" -> startsWith(bytes, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1);
            default -> true;
        };
        if (!valid) throw new IllegalArgumentException("文件内容与扩展名不匹配");
    }

    private boolean startsWith(byte[] bytes, int... expected) {
        if (bytes.length < expected.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if (unsigned(bytes[i]) != expected[i]) return false;
        }
        return true;
    }

    private boolean startsWithAscii(byte[] bytes, String text) {
        return asciiAt(bytes, 0, text);
    }

    private boolean asciiAt(byte[] bytes, int offset, String text) {
        byte[] expected = text.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        if (bytes.length < offset + expected.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if (bytes[offset + i] != expected[i]) return false;
        }
        return true;
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }
}
