package com.cqutcm.biomed.service;

import com.cqutcm.biomed.mapper.GrowthAnalysisMapper;
import com.cqutcm.biomed.mapper.SpectrumComparisonMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ResearchDataService {
    private final SpectrumComparisonMapper spectrumMapper;
    private final GrowthAnalysisMapper analysisMapper;

    public ResearchDataService(SpectrumComparisonMapper spectrumMapper, GrowthAnalysisMapper analysisMapper) {
        this.spectrumMapper = spectrumMapper;
        this.analysisMapper = analysisMapper;
    }

    public List<Map<String, Object>> listSpectrum() {
        return spectrumMapper.findAllAsMap();
    }

    public List<Map<String, Object>> listAnalysis() {
        return analysisMapper.findAllAsMap();
    }

    public Map<String, Object> createSpectrum(Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        LocalDateTime now = LocalDateTime.now();
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        spectrumMapper.insertMap(cleaned);
        return cleaned;
    }

    public Map<String, Object> createAnalysis(Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        LocalDateTime now = LocalDateTime.now();
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        analysisMapper.insertMap(cleaned);
        return cleaned;
    }

    public Map<String, Object> updateSpectrum(Map<String, Object> payload) {
        String id = id(payload, "spectrum comparison");
        Map<String, Object> cleaned = clean(payload);
        cleaned.put("id", id);
        spectrumMapper.updateMap(cleaned);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public Map<String, Object> updateAnalysis(Map<String, Object> payload) {
        String id = id(payload, "growth analysis");
        Map<String, Object> cleaned = clean(payload);
        cleaned.put("id", id);
        analysisMapper.updateMap(cleaned);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public void deleteSpectrum(String id) {
        if (spectrumMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("spectrum comparison not found");
        }
    }

    public void deleteAnalysis(String id) {
        if (analysisMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("growth analysis not found");
        }
    }

    public long spectrumCount() {
        return spectrumMapper.count();
    }

    public long analysisCount() {
        return analysisMapper.count();
    }

    private String id(Map<String, Object> payload, String label) {
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) {
            throw new IllegalArgumentException("missing id for " + label);
        }
        return id;
    }

    private Map<String, Object> clean(Map<String, Object> payload) {
        Map<String, Object> cleaned = new LinkedHashMap<>(payload);
        cleaned.remove("id");
        cleaned.remove("createdAt");
        cleaned.remove("updatedAt");
        cleaned.remove("_actorName");
        cleaned.remove("_actorRole");
        return cleaned;
    }
}
