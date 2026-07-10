package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.*;

public class GrowthAnalysisDTOs {
    public static class Create {
        @NotBlank @Size(max = 200) public String analysisName;
        @Size(max = 100) public String herbName;
        @Size(max = 100) public String district;
        @Size(max = 200) public String indicator;
        @Size(max = 200) public String baseline;
        @Size(max = 200) public String currentValue;
        @Size(max = 200) public String differenceDesc;
        @Size(max = 100) public String trend;
        @Size(max = 1000) public String conclusion;
    }
    public static class Update {
        @NotBlank public String id;
        @NotBlank @Size(max = 200) public String analysisName;
        @Size(max = 100) public String herbName;
        @Size(max = 100) public String district;
        @Size(max = 200) public String indicator;
        @Size(max = 200) public String baseline;
        @Size(max = 200) public String currentValue;
        @Size(max = 200) public String differenceDesc;
        @Size(max = 100) public String trend;
        @Size(max = 1000) public String conclusion;
        @NotNull public Integer version;
    }
    public static class Review {
        @NotBlank @Pattern(regexp = "^(已通过|已驳回|已发布|已归档)$") public String status;
        @Size(max = 500) public String reviewComment;
    }
}
