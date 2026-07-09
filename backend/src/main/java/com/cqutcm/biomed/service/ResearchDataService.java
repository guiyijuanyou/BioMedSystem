package com.cqutcm.biomed.service;

import com.cqutcm.biomed.repository.GenericRecordRepository;
import com.cqutcm.biomed.repository.ResearchDataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ResearchDataService {
    private final ResearchDataRepository researchRepository;
    private final GenericRecordRepository genericRepository;

    public ResearchDataService(ResearchDataRepository researchRepository, GenericRecordRepository genericRepository) {
        this.researchRepository = researchRepository;
        this.genericRepository = genericRepository;
    }

    public List<Map<String, Object>> listSpectrum() {
        seedSpectrumIfEmpty();
        return researchRepository.findSpectrum();
    }

    public List<Map<String, Object>> listAnalysis() {
        seedAnalysisIfEmpty();
        return researchRepository.findAnalysis();
    }

    public Map<String, Object> createSpectrum(Map<String, Object> payload) {
        seedSpectrumIfEmpty();
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        LocalDateTime now = LocalDateTime.now();
        researchRepository.insertSpectrum(id, cleaned, now);
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        return cleaned;
    }

    public Map<String, Object> createAnalysis(Map<String, Object> payload) {
        seedAnalysisIfEmpty();
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        LocalDateTime now = LocalDateTime.now();
        researchRepository.insertAnalysis(id, cleaned, now);
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        return cleaned;
    }

    public Map<String, Object> updateSpectrum(Map<String, Object> payload) {
        seedSpectrumIfEmpty();
        String id = id(payload, "spectrum comparison");
        Map<String, Object> cleaned = clean(payload);
        if (researchRepository.updateSpectrum(id, cleaned) == 0) {
            throw new IllegalArgumentException("spectrum comparison not found");
        }
        cleaned.put("id", id);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public Map<String, Object> updateAnalysis(Map<String, Object> payload) {
        seedAnalysisIfEmpty();
        String id = id(payload, "growth analysis");
        Map<String, Object> cleaned = clean(payload);
        if (researchRepository.updateAnalysis(id, cleaned) == 0) {
            throw new IllegalArgumentException("growth analysis not found");
        }
        cleaned.put("id", id);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public void deleteSpectrum(String id) {
        seedSpectrumIfEmpty();
        if (researchRepository.deleteSpectrum(id) == 0) {
            throw new IllegalArgumentException("spectrum comparison not found");
        }
    }

    public void deleteAnalysis(String id) {
        seedAnalysisIfEmpty();
        if (researchRepository.deleteAnalysis(id) == 0) {
            throw new IllegalArgumentException("growth analysis not found");
        }
    }

    public long spectrumCount() {
        seedSpectrumIfEmpty();
        return researchRepository.countSpectrum();
    }

    public long analysisCount() {
        seedAnalysisIfEmpty();
        return researchRepository.countAnalysis();
    }

    private void seedSpectrumIfEmpty() {
        if (researchRepository.countSpectrum() > 0) return;
        genericRepository.findByResourceType("spectrum-comparisons").forEach(record -> {
            if (!researchRepository.spectrumExists(record.getId())) {
                researchRepository.insertSpectrum(record.getId(), record.getPayload(), record.getCreatedAt() == null ? LocalDateTime.now() : record.getCreatedAt());
            }
        });
    }

    private void seedAnalysisIfEmpty() {
        if (researchRepository.countAnalysis() > 0) return;
        genericRepository.findByResourceType("growth-analysis").forEach(record -> {
            if (!researchRepository.analysisExists(record.getId())) {
                researchRepository.insertAnalysis(record.getId(), record.getPayload(), record.getCreatedAt() == null ? LocalDateTime.now() : record.getCreatedAt());
            }
        });
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
