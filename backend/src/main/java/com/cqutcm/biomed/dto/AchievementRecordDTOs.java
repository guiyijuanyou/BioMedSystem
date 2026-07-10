package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.*;

public class AchievementRecordDTOs {
    public static class Create {
        @NotBlank @Size(max = 200) public String title;
    }
    public static class Update {
        @NotBlank public String id;
        @NotBlank @Size(max = 200) public String title;
        @NotNull public Integer version;
    }
    public static class Review {
        @NotBlank @Pattern(regexp = "^(已通过|已驳回|已发布|已归档)$") public String status;
        @Size(max = 100) public String category;
        @Size(max = 100) public String level;
        @Size(max = 500) public String reviewComment;
    }
}
