package com.cqutcm.biomed.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * HPLC spectrum comparison engine.
 * Supports CSV parsing, normalization, interpolation alignment, and cosine similarity.
 */
@Service
public class SpectrumCompareService {

    private final ObjectMapper objectMapper;

    public SpectrumCompareService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** A single (x, y) data point. */
    public record DataPoint(double x, double y) {}

    /** Result of a comparison. */
    public record CompareResult(
            double similarity,
            String verdict,
            List<DataPoint> alignedSample,
            List<DataPoint> alignedReference,
            List<double[]> diffRegions,   // each: [xStart, xEnd, maxDiffPercent]
            int comparedPoints
    ) {}

    /**
     * Parse CSV content into data points.
     * Handles comma/tab/semicolon delimiters. Skips headers and comment lines.
     */
    public List<DataPoint> parseCSV(String csvText) {
        if (csvText == null || csvText.isBlank()) return List.of();
        String[] lines = csvText.trim().split("\\r?\\n");
        List<DataPoint> points = new ArrayList<>();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            // Skip header lines (first char is letter or Chinese or #)
            char first = line.charAt(0);
            if (Character.isLetter(first) || first == '#' || Character.UnicodeBlock.of(first) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                String[] parts = line.split("[,;\\t]");
                if (parts.length >= 2) {
                    try { Double.parseDouble(parts[0].trim()); } catch (NumberFormatException e) { continue; }
                } else { continue; }
            }
            String[] parts = line.split("[,;\\t]");
            if (parts.length >= 2) {
                try {
                    double x = Double.parseDouble(parts[0].trim());
                    double y = Double.parseDouble(parts[1].trim());
                    if (y >= 0) points.add(new DataPoint(x, y));
                } catch (NumberFormatException ignored) {}
            }
        }
        return points;
    }

    /** Parse data points from a JSON string. */
    public List<DataPoint> parseJSON(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            List<Map<String, Object>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream()
                    .map(m -> new DataPoint(
                            ((Number) m.getOrDefault("x", 0)).doubleValue(),
                            ((Number) m.getOrDefault("y", 0)).doubleValue()))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Serialize data points to JSON string. */
    public String toJSON(List<DataPoint> points) {
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            for (DataPoint p : points) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("x", p.x());
                m.put("y", p.y());
                list.add(m);
            }
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    /**
     * Normalize by total area (trapezoidal integration).
     * This eliminates injection volume differences.
     */
    public List<DataPoint> normalizeByArea(List<DataPoint> points) {
        double totalArea = 0;
        for (int i = 1; i < points.size(); i++) {
            totalArea += (points.get(i).y() + points.get(i - 1).y()) * (points.get(i).x() - points.get(i - 1).x()) / 2.0;
        }
        if (totalArea == 0) return points;
        double invArea = 1.0 / totalArea;
        return points.stream().map(p -> new DataPoint(p.x(), p.y() * invArea)).toList();
    }

    /**
     * Align two datasets to a common X-axis grid using linear interpolation.
     */
    public record AlignedData(double[] sampleY, double[] referenceY, double[] xAxis, double xMin, double xMax) {}

    public AlignedData alignAndInterpolate(List<DataPoint> sample, List<DataPoint> reference, int resolution) {
        double xMin = Math.max(sample.get(0).x(), reference.get(0).x());
        double xMax = Math.min(sample.get(sample.size() - 1).x(), reference.get(reference.size() - 1).x());
        double step = (xMax - xMin) / (resolution - 1);

        double[] xAxis = new double[resolution];
        double[] sY = new double[resolution];
        double[] rY = new double[resolution];

        for (int i = 0; i < resolution; i++) {
            double x = xMin + i * step;
            xAxis[i] = x;
            sY[i] = interpolate(sample, x);
            rY[i] = interpolate(reference, x);
        }
        return new AlignedData(sY, rY, xAxis, xMin, xMax);
    }

    private double interpolate(List<DataPoint> points, double tx) {
        if (tx <= points.get(0).x()) return points.get(0).y();
        if (tx >= points.get(points.size() - 1).x()) return points.get(points.size() - 1).y();

        int lo = 0, hi = points.size() - 1;
        while (lo < hi - 1) {
            int mid = (lo + hi) >>> 1;
            if (points.get(mid).x() <= tx) lo = mid; else hi = mid;
        }
        DataPoint left = points.get(lo), right = points.get(hi);
        double ratio = (tx - left.x()) / (right.x() - left.x());
        return left.y() + ratio * (right.y() - left.y());
    }

    /**
     * Cosine similarity: cos(theta) = sum(xi*yi) / sqrt(sum(xi^2) * sum(yi^2))
     */
    public double cosineSimilarity(double[] a, double[] b) {
        double sXY = 0, sX2 = 0, sY2 = 0;
        for (int i = 0; i < a.length; i++) {
            sXY += a[i] * b[i];
            sX2 += a[i] * a[i];
            sY2 += b[i] * b[i];
        }
        double denom = Math.sqrt(sX2) * Math.sqrt(sY2);
        return denom == 0 ? 0 : sXY / denom;
    }

    /**
     * Find regions where the normalized difference exceeds the threshold.
     *
     * Algorithm: scan left-to-right, mark diff-points, merge consecutive points
     * into groups, keep only groups with >= 3 consecutive diff-points (isolated
     * 1-2 point spikes are treated as noise, not systematic difference).
     * Baseline points (signal < 0.5% of max) are skipped.
     */
    public List<double[]> findDiffRegions(double[] xAxis, double[] sY, double[] rY, double threshold) {
        // Step 1: find global max signal for adaptive baseline threshold
        double maxSignal = 0;
        for (int i = 0; i < sY.length; i++) {
            maxSignal = Math.max(maxSignal, Math.abs(sY[i]));
            maxSignal = Math.max(maxSignal, Math.abs(rY[i]));
        }
        final double baselineFloor = maxSignal * 0.005;
        final int minConsecutive = 3;

        // Step 2: scan left-to-right, mark each point as diff or not
        boolean[] isDiff = new boolean[sY.length];
        for (int i = 0; i < sY.length; i++) {
            double signal = Math.max(Math.abs(sY[i]), Math.abs(rY[i]));
            if (signal < baselineFloor) continue; // skip baseline noise
            double diff = Math.abs(sY[i] - rY[i]) / signal;
            isDiff[i] = diff > threshold;
        }

        // Step 3: merge consecutive diff-points into groups, keep only >= minConsecutive
        List<double[]> regions = new ArrayList<>();
        int start = -1;
        double maxDiff = 0;
        for (int i = 0; i < isDiff.length; i++) {
            if (isDiff[i]) {
                if (start < 0) start = i;
                double signal = Math.max(Math.abs(sY[i]), Math.abs(rY[i]));
                double diff = Math.abs(sY[i] - rY[i]) / signal;
                maxDiff = Math.max(maxDiff, diff);
            } else {
                if (start >= 0) {
                    int count = i - start;
                    if (count >= minConsecutive) {
                        regions.add(new double[]{xAxis[start], xAxis[i - 1], Math.round(maxDiff * 1000.0) / 10.0});
                    }
                    start = -1; maxDiff = 0;
                }
            }
        }
        // Trailing group
        if (start >= 0) {
            int count = isDiff.length - start;
            if (count >= minConsecutive) {
                regions.add(new double[]{xAxis[start], xAxis[isDiff.length - 1], Math.round(maxDiff * 1000.0) / 10.0});
            }
        }
        return regions;
    }

    /**
     * Generate a verdict string based on similarity score.
     */
    public String verdict(double similarity) {
        if (similarity >= 0.90) return "通过 — 高度一致，达到标准品水平";
        else if (similarity >= 0.80) return "基本一致 — 存在可接受差异，建议复核";
        else return "不一致 — 差异显著，建议重新采样检测";
    }

    /**
     * Full comparison pipeline.
     */
    public CompareResult compare(List<DataPoint> sampleRaw, List<DataPoint> referenceRaw, int resolution, double diffThreshold) {
        // Sort by retention time first so interpolation works regardless of CSV row order
        List<DataPoint> sampleSorted = new ArrayList<>(sampleRaw);
        List<DataPoint> refSorted = new ArrayList<>(referenceRaw);
        sampleSorted.sort(Comparator.comparingDouble(DataPoint::x));
        refSorted.sort(Comparator.comparingDouble(DataPoint::x));

        List<DataPoint> sampleNorm = normalizeByArea(sampleSorted);
        List<DataPoint> refNorm = normalizeByArea(refSorted);
        AlignedData aligned = alignAndInterpolate(sampleNorm, refNorm, resolution);
        double similarity = cosineSimilarity(aligned.sampleY(), aligned.referenceY());
        List<double[]> diffs = findDiffRegions(aligned.xAxis(), aligned.sampleY(), aligned.referenceY(), diffThreshold);

        // Convert aligned arrays back to DataPoint lists for JSON serialization
        List<DataPoint> alignedSample = new ArrayList<>();
        List<DataPoint> alignedRef = new ArrayList<>();
        for (int i = 0; i < aligned.xAxis().length; i++) {
            alignedSample.add(new DataPoint(aligned.xAxis()[i], aligned.sampleY()[i]));
            alignedRef.add(new DataPoint(aligned.xAxis()[i], aligned.referenceY()[i]));
        }

        return new CompareResult(similarity, verdict(similarity), alignedSample, alignedRef, diffs, resolution);
    }
}
