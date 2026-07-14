package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BatchCatalogDTOs {
    public static class BatchCreate {
        @NotBlank public String herbId;
        @NotBlank @Size(max = 100) public String batchCode;
        @NotBlank @Size(max = 200) public String batchName;
        @Size(max = 100) public String traceCode;
        @Size(max = 200) public String plotName;
        @Size(max = 100) public String district;
        public BigDecimal longitude;
        public BigDecimal latitude;
        @Size(max = 100) public String scale;
        @Size(max = 2000) public String environment;
        public LocalDate plantingDate;
        public LocalDate expectedHarvestDate;
        @Size(max = 100) public String responsiblePerson;
        @Size(max = 50) public String currentStage;
    }

    public static class BatchUpdate extends BatchCreate {
        @NotBlank public String id;
        @NotNull public Integer version;
        @Size(max = 30) public String status;
    }

    public static class SampleCreate {
        @NotBlank public String batchId;
        @NotBlank @Size(max = 100) public String sampleCode;
        @Size(max = 100) public String sampleType;
        private LocalDateTime collectedAt;
        @Size(max = 100) public String collector;
        @Size(max = 200) public String sampleLocation;
        @Size(max = 300) public String storageCondition;
        @Size(max = 30) public String status;

        public LocalDateTime getCollectedAt() { return collectedAt; }

        public void setCollectedAt(Object value) {
            this.collectedAt = parseCollectedAt(value);
        }

        private static LocalDateTime parseCollectedAt(Object value) {
            if (value == null) return null;
            if (value instanceof LocalDateTime) return (LocalDateTime) value;
            String s = String.valueOf(value).trim();
            if (s.isEmpty()) return null;
            DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyy-M-d H:m:s"),
                DateTimeFormatter.ofPattern("yyyy-M-d H:m"),
                DateTimeFormatter.ofPattern("yyyy-M-d")
            };
            for (DateTimeFormatter f : formatters) {
                try { return LocalDateTime.parse(s, f); } catch (DateTimeParseException ignored) {}
            }
            throw new IllegalArgumentException("无法解析采样时间格式: " + s);
        }
    }

    public static class SampleUpdate extends SampleCreate {
        @NotBlank public String id;
        @NotNull public Integer version;
    }
}
