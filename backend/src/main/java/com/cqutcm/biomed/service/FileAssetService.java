package com.cqutcm.biomed.service;

import com.cqutcm.biomed.model.FileAsset;
import com.cqutcm.biomed.repository.FileAssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private final FileAssetRepository repository;
    private final Path uploadDir;

    public FileAssetService(FileAssetRepository repository, @Value("${app.upload-dir:data/uploads}") String uploadDir) {
        this.repository = repository;
        this.uploadDir = Path.of(uploadDir);
    }

    public List<FileAsset> list() {
        return repository.findAll();
    }

    public Optional<FileAsset> findById(String id) {
        return repository.findById(id);
    }

    public FileAsset upload(Map<String, String> request) {
        try {
            Files.createDirectories(uploadDir);
            String id = UUID.randomUUID().toString();
            String fileName = request.getOrDefault("fileName", "upload.bin").replaceAll("[\\\\/:*?\"<>|]", "_");
            byte[] content = Base64.getDecoder().decode(request.getOrDefault("contentBase64", ""));
            Path target = uploadDir.resolve(id + "-" + fileName);
            Files.write(target, content);

            FileAsset file = new FileAsset();
            file.setId(id);
            file.setFileName(fileName);
            file.setCategory(request.getOrDefault("category", "教学资料"));
            file.setSize(content.length);
            file.setPath(target.toString());
            file.setCreatedAt(LocalDateTime.now());
            repository.insert(file);
            return file;
        } catch (Exception ex) {
            throw new IllegalStateException("文件上传失败", ex);
        }
    }
}

