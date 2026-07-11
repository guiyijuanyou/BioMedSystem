package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class GrowthAnalysisDTOs {
    public static class Create {
        public String batchId;
        @NotBlank @Size(max = 200) public String analysisName;
        @Size(max = 100) public String herbName;
        @Size(max = 100) public String district;
        @Size(max = 200) public String indicator;
        @Size(max = 200) public String baseline;
        @Size(max = 200) public String currentValue;
        @Size(max = 200) public String differenceDesc;
        @Size(max = 100) public String trend;
        @Size(max = 1000) public String conclusion;
        @Size(max = 100) public String analyst;
        public LocalDateTime analyzedAt;
    }
    public static class Update {
        @NotBlank public String id;
        public String batchId;
        @NotBlank @Size(max = 200) public String analysisName;
        @Size(max = 100) public String herbName;
        @Size(max = 100) public String district;
        @Size(max = 200) public String indicator;
        @Size(max = 200) public String baseline;
        @Size(max = 200) public String currentValue;
        @Size(max = 200) public String differenceDesc;
        @Size(max = 100) public String trend;
        @Size(max = 1000) public String conclusion;
        @Size(max = 100) public String analyst;
        public LocalDateTime analyzedAt;
        @NotNull public Integer version;
    }
    public static class Review {
        @NotBlank @Pattern(regexp = "^(已通过|已驳回|已发布|已归档)$") public String status;
        @Size(max = 500) public String reviewComment;
    }
}
