package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.*;

public class TeachingResourceDTOs {
    public static class Create {
        @Size(max = 64) public String courseId;
        @NotBlank @Size(max = 200) public String courseTitle;
        @NotBlank @Size(max = 200) public String title;
        @NotBlank @Size(max = 80) public String resourceType;
        @Size(max = 64) public String fileId;
        @Size(max = 500) public String videoUrl;
        @Size(max = 500) public String fileUrl;
    }
    public static class Update {
        @NotBlank public String id;
        @Size(max = 64) public String courseId;
        @NotBlank @Size(max = 200) public String courseTitle;
        @NotBlank @Size(max = 200) public String title;
        @NotBlank @Size(max = 80) public String resourceType;
        @Size(max = 64) public String fileId;
        @Size(max = 500) public String videoUrl;
        @Size(max = 500) public String fileUrl;
        @NotNull public Integer version;
    }
    public static class Review {
        @NotBlank @Pattern(regexp = "^(已通过|已驳回|已发布|已归档)$") public String status;
        @Size(max = 500) public String reviewComment;
    }
}
