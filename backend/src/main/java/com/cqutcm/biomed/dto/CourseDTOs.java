package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.*;

public class CourseDTOs {
    public static class Create {
        @NotBlank @Size(max = 200) public String title;
        @Size(max = 100) public String materialType;
        @DecimalMin("0.0") @DecimalMax("9999.9") public java.math.BigDecimal hours;
    }
    public static class Update {
        @NotBlank public String id;
        @NotBlank @Size(max = 200) public String title;
        @Size(max = 100) public String materialType;
        @DecimalMin("0.0") @DecimalMax("9999.9") public java.math.BigDecimal hours;
        @NotNull public Integer version;
    }
    public static class Review {
        @NotBlank @Pattern(regexp = "^(已通过|已驳回|已发布|已归档)$") public String status;
        @Size(max = 500) public String reviewComment;
    }
}
