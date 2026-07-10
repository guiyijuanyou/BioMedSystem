package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.FileAsset;
import com.cqutcm.biomed.mapper.FileAssetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileAssetService {
    private final FileAssetMapper mapper;
    private final Path uploadDir;

    public FileAssetService(FileAssetMapper mapper, @Value("${app.upload-dir:data/uploads}") String uploadDir) {
        this.mapper = mapper;
        this.uploadDir = Path.of(uploadDir);
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
            Files.createDirectories(uploadDir);
            String id = UUID.randomUUID().toString();
            String originalName = upload.getOriginalFilename() == null || upload.getOriginalFilename().isBlank()
                    ? "upload.bin"
                    : upload.getOriginalFilename();
            String fileName = sanitizeFileName(originalName);
            Path target = uploadDir.resolve(id + "-" + fileName);
            upload.transferTo(target);
            return saveFile(id, fileName, category, upload.getSize(), target);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("文件上传失败", ex);
        }
    }

    public FileAsset uploadBase64(Map<String, String> request) {
        try {
            Files.createDirectories(uploadDir);
            String id = UUID.randomUUID().toString();
            String fileName = sanitizeFileName(request.getOrDefault("fileName", "upload.bin"));
            byte[] content = Base64.getDecoder().decode(request.getOrDefault("contentBase64", ""));
            Path target = uploadDir.resolve(id + "-" + fileName);
            Files.write(target, content);
            return saveFile(id, fileName, request.get("category"), content.length, target);
        } catch (Exception ex) {
            throw new IllegalStateException("文件上传失败", ex);
        }
    }

    private FileAsset saveFile(String id, String fileName, String category, long size, Path target) {
        FileAsset file = new FileAsset();
        file.setId(id);
        file.setFileName(fileName);
        file.setCategory(category == null || category.isBlank() ? "教学资料" : category);
        file.setSizeBytes(size);
        file.setStoragePath(target.toString());
        file.setCreatedAt(LocalDateTime.now());
        mapper.insert(file);
        return file;
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
