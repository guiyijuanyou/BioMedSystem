package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.*;

public class ResearchProjectDTOs {
    public static class Create {
        @NotBlank @Size(max = 200) public String title;
        @Size(max = 2000) public String requirements;
        @Size(max = 100) public String stage;
        @Size(max = 2000) public String transformation;
    }
    public static class Update {
        @NotBlank public String id;
        @NotBlank @Size(max = 200) public String title;
        @Size(max = 2000) public String requirements;
        @Size(max = 100) public String stage;
        @Size(max = 2000) public String transformation;
        @NotNull public Integer version;
    }
    public static class Review {
        @NotBlank @Pattern(regexp = "^(已通过|已驳回|已发布|已归档)$") public String status;
        @Size(max = 500) public String reviewComment;
    }
}
