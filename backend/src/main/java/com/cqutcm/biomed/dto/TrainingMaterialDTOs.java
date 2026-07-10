package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.*;

public class TrainingMaterialDTOs {
    public static class Create {
        @NotBlank @Size(max = 200) public String title;
        @Size(max = 200) public String audience;
        @Size(max = 1000) public String tracking;
    }
    public static class Update {
        @NotBlank public String id;
        @NotBlank @Size(max = 200) public String title;
        @Size(max = 200) public String audience;
        @Size(max = 1000) public String tracking;
        @NotNull public Integer version;
    }
    public static class Review {
        @NotBlank @Pattern(regexp = "^(已通过|已驳回|已发布|已归档)$") public String status;
        @Size(max = 500) public String reviewComment;
    }
}
