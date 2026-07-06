package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.model.FileAsset;
import com.cqutcm.biomed.service.FileAssetService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileAssetService service;

    public FileController(FileAssetService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, Object> list() {
        return Map.of("items", service.list());
    }

    @PostMapping("/upload")
    public FileAsset upload(@RequestBody Map<String, String> request) {
        return service.upload(request);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        FileAsset file = getExistingFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(new FileSystemResource(Path.of(file.getPath())));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(@PathVariable String id) {
        FileAsset file = getExistingFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(detectContentType(file.getFileName())))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .body(new FileSystemResource(Path.of(file.getPath())));
    }

    private FileAsset getExistingFile(String id) {
        FileAsset file = service.findById(id).orElseThrow(() -> new IllegalArgumentException("未找到文件"));
        if (!new FileSystemResource(Path.of(file.getPath())).exists()) {
            throw new IllegalArgumentException("文件不存在");
        }
        return file;
    }

    private String detectContentType(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".txt")) return "text/plain; charset=utf-8";
        if (lower.endsWith(".html")) return "text/html; charset=utf-8";
        return "application/octet-stream";
    }
}
