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
            @RequestParam(value = "spectrumType", required = false, defaultValue = "HPLC") String spectrumType,
            @RequestParam(value = "algorithm", required = false, defaultValue = "COSINE") String algorithm,
            @RequestParam(value = "resolution", required = false, defaultValue = "500") int resolution,
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
        SpectrumCompareService.CompareResult result = compareService.compare(samplePoints, referencePoints, resolution);
        double score = Math.round(result.similarity() * 10000.0) / 100.0;

        // Build diff regions for response
        List<Map<String, Object>> diffList = new ArrayList<>();
        for (double[] d : result.diffRegions()) {
            Map<String, Object> dr = new LinkedHashMap<>();
            dr.put("xStart", Math.round(d[0] * 10.0) / 10.0);
            dr.put("xEnd", Math.round(d[1] * 10.0) / 10.0);
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
     * List records marked as reference standards.
     */
    @GetMapping("/references")
    public Map<String, Object> listReferences(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireActor(authorization);
        // References are records that have data points and are either marked as standard or imported
        List<Map<String, Object>> all = spectrumMapper.findAllAsMap();
        List<Map<String, Object>> refs = new ArrayList<>();
        for (Map<String, Object> item : all) {
            String status = String.valueOf(item.getOrDefault("status", ""));
            String sampleData = String.valueOf(item.getOrDefault("sampleDataJson", ""));
            String refData = String.valueOf(item.getOrDefault("referenceDataJson", ""));
            if ("REFERENCE".equals(status) || (!"null".equals(sampleData) && !"[]".equals(sampleData)) || (!"null".equals(refData) && !"[]".equals(refData))) {
                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("id", item.get("id"));
                summary.put("herbName", item.get("herbName"));
                summary.put("referenceName", item.get("referenceName"));
                summary.put("spectrumType", item.get("spectrumType"));
                summary.put("sampleCode", item.get("sampleCode"));
                summary.put("createdAt", item.get("createdAt"));
                refs.add(summary);
            }
        }
        return Map.of("items", refs);
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
