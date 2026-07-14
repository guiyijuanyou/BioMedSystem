package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.mapper.SpectrumComparisonMapper;
import com.cqutcm.biomed.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/spectrum")
public class SpectrumCompareController {

    private final AuthService authService;
    private final SpectrumCompareService compareService;
    private final SpectrumComparisonMapper spectrumMapper;
    private final PermissionService permissionService;
    private final BatchCatalogService batchCatalogService;

    public SpectrumCompareController(AuthService authService, SpectrumCompareService compareService,
                                      SpectrumComparisonMapper spectrumMapper,
                                      PermissionService permissionService, BatchCatalogService batchCatalogService) {
        this.authService = authService;
        this.compareService = compareService;
        this.spectrumMapper = spectrumMapper;
        this.permissionService = permissionService;
        this.batchCatalogService = batchCatalogService;
    }

    /**
     * Upload and parse a CSV file. Returns parsed data points for preview.
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authorization) throws IOException {
        authService.requireActor(authorization);

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        // Try GBK if UTF-8 fails for some chars
        if (content.contains("�")) {
            content = new String(file.getBytes(), java.nio.charset.Charset.forName("GBK"));
        }

        List<SpectrumCompareService.DataPoint> points = compareService.parseCSV(content);
        if (points.size() < 10) {
            throw new IllegalArgumentException("数据点太少（" + points.size() + " 个），至少需要 10 个数据点");
        }

        double xMin = points.get(0).x(), xMax = points.get(points.size() - 1).x();
        double yMin = points.stream().mapToDouble(SpectrumCompareService.DataPoint::y).min().orElse(0);
        double yMax = points.stream().mapToDouble(SpectrumCompareService.DataPoint::y).max().orElse(0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dataPointsJson", compareService.toJSON(points));
        result.put("pointCount", points.size());
        result.put("xMin", xMin);
        result.put("xMax", xMax);
        result.put("yMin", yMin);
        result.put("yMax", yMax);
        result.put("fileName", file.getOriginalFilename());
        return result;
    }

    /**
     * Execute a spectrum comparison.
     * Accepts sample CSV file or existing sample data, plus reference selection.
     */
    @PostMapping("/compare")
    public Map<String, Object> compare(
            @RequestParam(value = "sampleFile", required = false) MultipartFile sampleFile,
            @RequestParam(value = "referenceFile", required = false) MultipartFile referenceFile,
            @RequestParam(value = "sampleId", required = false) String sampleId,
            @RequestParam(value = "referenceId", required = false) String referenceId,
            @RequestParam(value = "herbName", required = false, defaultValue = "") String herbName,
            @RequestParam(value = "sampleCode", required = false, defaultValue = "") String sampleCode,
            @RequestParam(value = "district", required = false, defaultValue = "") String district,
            @RequestParam(value = "remark", required = false, defaultValue = "") String remark,
            @RequestParam(value = "spectrumType", required = false, defaultValue = "HPLC") String spectrumType,
            @RequestParam(value = "algorithm", required = false, defaultValue = "COSINE") String algorithm,
            @RequestParam(value = "resolution", required = false, defaultValue = "500") int resolution,
            @RequestParam(value = "diffThreshold", required = false, defaultValue = "0.06") double diffThreshold,
            @RequestHeader(value = "Authorization", required = false) String authorization) throws IOException {

        PermissionService.Actor actor = authService.requireActor(authorization);

        // Load sample data
        List<SpectrumCompareService.DataPoint> samplePoints;
        if (sampleFile != null && !sampleFile.isEmpty()) {
            String content = readFile(sampleFile);
            samplePoints = compareService.parseCSV(content);
        } else if (sampleId != null && !sampleId.isBlank()) {
            Map<String, Object> existing = spectrumMapper.findByIdAsMap(sampleId);
            if (existing == null) throw new IllegalArgumentException("样本记录不存在: " + sampleId);
            String json = String.valueOf(existing.getOrDefault("sampleDataJson", "[]"));
            samplePoints = compareService.parseJSON(json);
            if (samplePoints.isEmpty()) throw new IllegalArgumentException("样本记录中无图谱数据点");
            if (herbName.isBlank()) herbName = String.valueOf(existing.getOrDefault("herbName", ""));
            if (sampleCode.isBlank()) sampleCode = String.valueOf(existing.getOrDefault("sampleCode", ""));
            if (district.isBlank()) district = String.valueOf(existing.getOrDefault("district", ""));
        } else {
            throw new IllegalArgumentException("请上传样本 CSV 文件或选择已有样本");
        }
        if (samplePoints.size() < 10) throw new IllegalArgumentException("样本数据点不足（" + samplePoints.size() + " 个）");

        // Load reference data
        List<SpectrumCompareService.DataPoint> referencePoints;
        String referenceName = "";
        if (referenceFile != null && !referenceFile.isEmpty()) {
            String content = readFile(referenceFile);
            referencePoints = compareService.parseCSV(content);
            referenceName = referenceFile.getOriginalFilename();
        } else if (referenceId != null && !referenceId.isBlank()) {
            Map<String, Object> existing = spectrumMapper.findByIdAsMap(referenceId);
            if (existing == null) throw new IllegalArgumentException("标准品记录不存在: " + referenceId);
            // Try reference_data_json first, then sample_data_json (if the reference is stored as a sample)
            String json = String.valueOf(existing.getOrDefault("referenceDataJson", "[]"));
            if ("[]".equals(json) || "null".equals(json)) {
                json = String.valueOf(existing.getOrDefault("sampleDataJson", "[]"));
            }
            referencePoints = compareService.parseJSON(json);
            if (referencePoints.isEmpty()) throw new IllegalArgumentException("标准品记录中无图谱数据点");
            referenceName = String.valueOf(existing.getOrDefault("referenceName",
                    existing.getOrDefault("herbName", "标准品")));
        } else {
            throw new IllegalArgumentException("请上传标准品 CSV 文件或选择已有标准品");
        }
        if (referencePoints.size() < 10) throw new IllegalArgumentException("标准品数据点不足");

        // Check X-range overlap
        double sampleXMin = samplePoints.get(0).x(), sampleXMax = samplePoints.get(samplePoints.size() - 1).x();
        double refXMin = referencePoints.get(0).x(), refXMax = referencePoints.get(referencePoints.size() - 1).x();
        double overlapMin = Math.max(sampleXMin, refXMin);
        double overlapMax = Math.min(sampleXMax, refXMax);
        if (overlapMin >= overlapMax) {
            throw new IllegalArgumentException("样本和标准品的 X 轴范围无交集（样本: " +
                    String.format("%.1f-%.1f", sampleXMin, sampleXMax) + ", 标准品: " +
                    String.format("%.1f-%.1f", refXMin, refXMax) + "），无法比对");
        }

        // Execute comparison
        SpectrumCompareService.CompareResult result = compareService.compare(samplePoints, referencePoints, resolution, diffThreshold);
        double score = Math.round(result.similarity() * 10000.0) / 100.0;

        // Build diff regions for response
        List<Map<String, Object>> diffList = new ArrayList<>();
        for (double[] d : result.diffRegions()) {
            Map<String, Object> dr = new LinkedHashMap<>();
            dr.put("xStart", Math.round(d[0] * 100.0) / 100.0);
            dr.put("xEnd", Math.round(d[1] * 100.0) / 100.0);
            dr.put("maxDiffPercent", Math.round(d[2] * 10.0) / 10.0);
            diffList.add(dr);
        }

        // Save comparison record
        String recordId = UUID.randomUUID().toString();
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", recordId);
        record.put("herbName", herbName.isBlank() ? "未知" : herbName);
        record.put("sampleCode", sampleCode);
        record.put("district", district);
        record.put("remark", remark);
        record.put("spectrumType", spectrumType);
        record.put("referenceName", referenceName);
        record.put("similarity", new BigDecimal(String.valueOf(score)));
        record.put("result", result.verdict());
        record.put("operatorName", actor.name());
        record.put("comparedAt", LocalDateTime.now().toString());
        record.put("sampleDataJson", compareService.toJSON(samplePoints));
        record.put("referenceDataJson", compareService.toJSON(referencePoints));
        record.put("compareAlgorithm", algorithm);
        record.put("status", "已完成");
        record.put("createdAt", LocalDateTime.now().toString());
        spectrumMapper.insertMap(record);


        // Build response
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", recordId);
        resp.put("similarity", score);
        resp.put("verdict", result.verdict());
        resp.put("algorithm", algorithm);
        resp.put("comparedPoints", resolution);
        resp.put("diffRegions", diffList);
        resp.put("alignedSample", compareService.toJSON(result.alignedSample()));
        resp.put("alignedReference", compareService.toJSON(result.alignedReference()));
        resp.put("xMin", result.alignedSample().get(0).x());
        resp.put("xMax", result.alignedSample().get(result.alignedSample().size() - 1).x());
        resp.put("herbName", herbName);
        resp.put("referenceName", referenceName);
        return resp;
    }

    /**
     * List reference standards: public (REFERENCE) + current user's private (PRIVATE_REF).
     */
    @GetMapping("/references")
    public Map<String, Object> listReferences(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        List<Map<String, Object>> all = spectrumMapper.findAllAsMap();
        List<Map<String, Object>> refs = new ArrayList<>();
        for (Map<String, Object> item : all) {
            String status = String.valueOf(item.getOrDefault("status", ""));
            String refData = String.valueOf(item.getOrDefault("referenceDataJson", ""));
            String sampleData = String.valueOf(item.getOrDefault("sampleDataJson", ""));
            boolean hasData = (!"null".equals(refData) && !"[]".equals(refData) && refData.length() > 10)
                           || (!"null".equals(sampleData) && !"[]".equals(sampleData) && sampleData.length() > 10);
            if (!hasData) continue;
            // Public references visible to all; private references only to owner
            if ("REFERENCE".equals(status) || ("PRIVATE_REF".equals(status) && actor.name().equals(String.valueOf(item.getOrDefault("operatorName", ""))))) {
                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("id", item.get("id"));
                summary.put("herbName", item.getOrDefault("herbName", ""));
                summary.put("referenceName", item.getOrDefault("referenceName", ""));
                summary.put("spectrumType", item.getOrDefault("spectrumType", "HPLC"));
                summary.put("createdAt", item.get("createdAt"));
                summary.put("status", status);
                refs.add(summary);
            }
        }
        return Map.of("items", refs);
    }

    /** Current user's uploaded sample CSV history (last 20). */
    @GetMapping("/my-samples")
    public Map<String, Object> mySamples(@RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        List<Map<String, Object>> all = spectrumMapper.findAllAsMap();
        List<Map<String, Object>> samples = new ArrayList<>();
        for (Map<String, Object> item : all) {
            if (!actor.name().equals(String.valueOf(item.getOrDefault("operatorName", "")))) continue;
            String sd = String.valueOf(item.getOrDefault("sampleDataJson", ""));
            if (!"null".equals(sd) && !"[]".equals(sd) && sd.length() > 10) {
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("id", item.get("id"));
                s.put("herbName", item.getOrDefault("herbName", ""));
                s.put("sampleCode", item.getOrDefault("sampleCode", ""));
                s.put("createdAt", item.get("createdAt"));
                samples.add(s);
            }
        }
        // Sort by time desc, limit 20
        samples.sort((a, b) -> String.valueOf(b.getOrDefault("createdAt", ""))
                .compareTo(String.valueOf(a.getOrDefault("createdAt", ""))));
        if (samples.size() > 20) samples = samples.subList(0, 20);
        return Map.of("items", samples);
    }

    /** Current user's private reference CSV history. */
    @GetMapping("/my-references")
    public Map<String, Object> myReferences(@RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        List<Map<String, Object>> all = spectrumMapper.findAllAsMap();
        List<Map<String, Object>> refs = new ArrayList<>();
        for (Map<String, Object> item : all) {
            if (!"PRIVATE_REF".equals(String.valueOf(item.getOrDefault("status", "")))) continue;
            if (!actor.name().equals(String.valueOf(item.getOrDefault("operatorName", "")))) continue;
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", item.get("id"));
            r.put("referenceName", item.getOrDefault("referenceName", ""));
            r.put("herbName", item.getOrDefault("herbName", ""));
            r.put("createdAt", item.get("createdAt"));
            refs.add(r);
        }
        return Map.of("items", refs);
    }

    /** Admin: upload a public reference standard. */
    @PostMapping("/reference")
    public Map<String, Object> uploadReference(
            @RequestParam("file") MultipartFile file,
            @RequestParam("referenceName") String referenceName,
            @RequestParam(value = "herbName", required = false, defaultValue = "") String herbName,
            @RequestHeader(value = "Authorization", required = false) String authorization) throws IOException {
        PermissionService.Actor actor = authService.requireActor(authorization);
        if (!"admin".equals(actor.role())) throw new AuthorizationDeniedException("仅管理员可上传公共标准品");

        String content = readFile(file);
        List<SpectrumCompareService.DataPoint> points = compareService.parseCSV(content);
        if (points.size() < 10) throw new IllegalArgumentException("数据点太少（" + points.size() + " 个）");

        String id = UUID.randomUUID().toString();
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", id);
        record.put("referenceName", referenceName);
        record.put("herbName", herbName.isBlank() ? referenceName : herbName);
        record.put("spectrumType", "HPLC");
        record.put("referenceDataJson", compareService.toJSON(points));
        record.put("operatorName", actor.name());
        record.put("status", "REFERENCE");
        record.put("createdAt", LocalDateTime.now().toString());
        spectrumMapper.insertMap(record);
        return Map.of("id", id, "referenceName", referenceName, "herbName", herbName, "pointCount", points.size());
    }

    /** Clear current user's sample history. */
    @DeleteMapping("/my-samples")
    public Map<String, Object> clearMySamples(@RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        List<Map<String, Object>> all = spectrumMapper.findAllAsMap();
        int count = 0;
        for (Map<String, Object> item : all) {
            if (!actor.name().equals(String.valueOf(item.getOrDefault("operatorName", "")))) continue;
            String sd = String.valueOf(item.getOrDefault("sampleDataJson", ""));
            if (!"null".equals(sd) && !"[]".equals(sd) && sd.length() > 10) {
                spectrumMapper.deleteById(String.valueOf(item.get("id")));
                count++;
            }
        }
        return Map.of("message", "deleted " + count + " sample records");
    }

    /** Clear current user's private reference history. */
    @DeleteMapping("/my-references")
    public Map<String, Object> clearMyReferences(@RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        List<Map<String, Object>> all = spectrumMapper.findAllAsMap();
        int count = 0;
        for (Map<String, Object> item : all) {
            if (!"PRIVATE_REF".equals(String.valueOf(item.getOrDefault("status", "")))) continue;
            if (!actor.name().equals(String.valueOf(item.getOrDefault("operatorName", "")))) continue;
            spectrumMapper.deleteById(String.valueOf(item.get("id")));
            count++;
        }
        return Map.of("message", "deleted " + count + " reference records");
    }

    /** Delete a reference (admin: any; owner: own PRIVATE_REF only). */
    @DeleteMapping("/reference/{id}")
    public Map<String, Object> deleteReference(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        Map<String, Object> existing = spectrumMapper.findByIdAsMap(id);
        if (existing == null) throw new IllegalArgumentException("标准品不存在");
        String status = String.valueOf(existing.getOrDefault("status", ""));
        String owner = String.valueOf(existing.getOrDefault("operatorName", ""));
        if ("REFERENCE".equals(status) && !"admin".equals(actor.role()))
            throw new AuthorizationDeniedException("仅管理员可删除公共标准品");
        if ("PRIVATE_REF".equals(status) && !actor.name().equals(owner) && !"admin".equals(actor.role()))
            throw new AuthorizationDeniedException("仅上传者本人可删除");
        spectrumMapper.deleteById(id);
        return Map.of("message", "deleted", "id", id);
    }

    /**
     * Get full comparison detail including aligned data for chart rendering.
     */
    @GetMapping("/comparisons/{id}")
    public Map<String, Object> getComparison(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireActor(authorization);
        Map<String, Object> record = spectrumMapper.findByIdAsMap(id);
        if (record == null) throw new IllegalArgumentException("比对记录不存在: " + id);
        return record;
    }

    private String readFile(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        if (content.contains("�")) {
            content = new String(file.getBytes(), java.nio.charset.Charset.forName("GBK"));
        }
        return content;
    }
}
