package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.entity.FileAsset;
import com.cqutcm.biomed.service.AuthService;
import com.cqutcm.biomed.service.AuthorizationDeniedException;
import com.cqutcm.biomed.service.FileAssetService;
import com.cqutcm.biomed.service.PermissionService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileAssetService service;
    private final AuthService authService;

    public FileController(FileAssetService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = AuthService.SESSION_COOKIE, required = false) String sessionCookie
    ) {
        requireActor(authorization, sessionCookie);
        List<Map<String, Object>> items = service.list().stream().map(this::fileView).toList();
        return Map.of("items", items);
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = AuthService.SESSION_COOKIE, required = false) String sessionCookie
    ) {
        requireUploader(authorization, sessionCookie);
        return fileView(service.upload(file, category));
    }

    @PostMapping(value = "/upload-json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> uploadJson(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = AuthService.SESSION_COOKIE, required = false) String sessionCookie
    ) {
        requireUploader(authorization, sessionCookie);
        return fileView(service.uploadBase64(request));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = AuthService.SESSION_COOKIE, required = false) String sessionCookie
    ) {
        requireActor(authorization, sessionCookie);
        FileAsset file = getExistingFile(id);
        Path stored = service.requireStoredFile(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(CacheControl.noStore().cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName(file))
                .header("X-Content-Type-Options", "nosniff")
                .header("Cross-Origin-Resource-Policy", "same-origin")
                .body(new FileSystemResource(stored));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = AuthService.SESSION_COOKIE, required = false) String sessionCookie
    ) {
        requireActor(authorization, sessionCookie);
        FileAsset file = getExistingFile(id);
        Path stored = service.requireStoredFile(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(detectContentType(file.getFileName())))
                .cacheControl(CacheControl.noStore().cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFileName(file))
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "sandbox; default-src 'none'")
                .header("Cross-Origin-Resource-Policy", "same-origin")
                .body(new FileSystemResource(stored));
    }

    private FileAsset getExistingFile(String id) {
        return service.findById(id).orElseThrow(() -> new IllegalArgumentException("未找到文件"));
    }

    private String encodedFileName(FileAsset file) {
        return URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
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
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".txt")) return "text/plain; charset=utf-8";
        if (lower.endsWith(".csv")) return "text/csv; charset=utf-8";
        return "application/octet-stream";
    }

    private PermissionService.Actor requireActor(String authorization, String sessionCookie) {
        return authService.requireActor(authorization, sessionCookie);
    }

    private void requireUploader(String authorization, String sessionCookie) {
        PermissionService.Actor actor = requireActor(authorization, sessionCookie);
        if (!List.of("admin", "teacher", "researcher").contains(actor.role())) {
            throw new AuthorizationDeniedException("current role cannot upload files");
        }
    }

    private Map<String, Object> fileView(FileAsset file) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", file.getId());
        view.put("fileName", file.getFileName());
        view.put("category", file.getCategory());
        view.put("size", file.getSizeBytes());
        view.put("sizeBytes", file.getSizeBytes());
        view.put("createdAt", file.getCreatedAt());
        view.put("previewUrl", "/api/files/" + file.getId() + "/preview");
        view.put("downloadUrl", "/api/files/" + file.getId() + "/download");
        return view;
    }
}
