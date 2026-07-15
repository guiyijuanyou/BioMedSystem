package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.entity.FileAsset;
import com.cqutcm.biomed.entity.HerbEncyclopedia;
import com.cqutcm.biomed.mapper.HerbEncyclopediaMapper;
import com.cqutcm.biomed.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/herb-encyclopedia")
public class HerbEncyclopediaController {

    private final AuthService authService;
    private final FileAssetService fileAssetService;
    private final HerbEncyclopediaMapper mapper;
    private final ObjectMapper objectMapper;

    public HerbEncyclopediaController(AuthService authService, FileAssetService fileAssetService,
                                       HerbEncyclopediaMapper mapper, ObjectMapper objectMapper) {
        this.authService = authService;
        this.fileAssetService = fileAssetService;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/by-name/{name}")
    public HerbEncyclopedia getByName(@PathVariable String name) {
        HerbEncyclopedia herb = mapper.findByName(name);
        if (herb == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到该药材");
        }
        return herb;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> importData(
            @RequestParam("data") String dataJson,
            @RequestPart("images") MultipartFile[] images,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        if (!"admin".equals(actor.role())) {
            throw new AuthorizationDeniedException("仅管理员可导入百科数据");
        }

        // Upload images first, build filename -> fileId map
        Map<String, String> imageFileIds = new HashMap<>();
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                FileAsset asset = fileAssetService.upload(image, "图片资料");
                imageFileIds.put(image.getOriginalFilename(), asset.getId());
            }
        }

        // Parse JSON data
        List<Map<String, Object>> entries;
        try {
            entries = objectMapper.readValue(dataJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 数据格式错误: " + e.getMessage());
        }

        int imported = 0;
        int skipped = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Map<String, Object> entry : entries) {
            String name = str(entry, "name");
            if (name == null || name.isBlank()) {
                skipped++;
                continue;
            }
            // Check duplicate by name
            HerbEncyclopedia existing = mapper.findByName(name);
            if (existing != null) {
                skipped++;
                continue;
            }

            HerbEncyclopedia h = new HerbEncyclopedia();
            h.setId(UUID.randomUUID().toString());
            h.setName(name);
            h.setPinyin(str(entry, "pinyin"));
            h.setEnglishName(str(entry, "englishName"));
            h.setLatinName(str(entry, "latinName"));
            h.setCategory(str(entry, "category"));
            h.setSourceDesc(str(entry, "sourceDesc"));
            h.setOriginDesc(str(entry, "originDesc"));
            h.setMacroscopic(str(entry, "macroscopic"));
            h.setQualityDesc(str(entry, "qualityDesc"));
            h.setNatureFlavor(str(entry, "natureFlavor"));
            h.setEfficacy(str(entry, "efficacy"));
            h.setSourceUrl(str(entry, "sourceUrl"));

            // Match image by filename
            String imageFile = str(entry, "imageFile");
            if (imageFile != null && !imageFile.isBlank()) {
                String fileId = imageFileIds.get(imageFile);
                if (fileId != null) {
                    h.setImageFileId(fileId);
                }
            }

            h.setVersion(0);
            h.setCreatedAt(now);
            mapper.insert(h);
            imported++;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("message", "导入完成：" + imported + " 条新增，" + skipped + " 条跳过");
        return result;
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : String.valueOf(v);
    }
}
