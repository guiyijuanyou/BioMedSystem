package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class EvaluationRecordDTOs {
    public static class Create {
        @NotBlank @Size(max = 100) public String herbName;
        @Size(max = 200) public String indicator;
        @DecimalMin("0.00") @DecimalMax("100.00") public BigDecimal score;
        @Size(max = 200) public String result;
        @Size(max = 1000) public String applicationMaterial;
        @NotBlank @Size(max = 100) public String subjectOwner;
    }
    public static class Update {
        @NotBlank public String id;
        @NotBlank @Size(max = 100) public String herbName;
        @Size(max = 200) public String indicator;
        @DecimalMin("0.00") @DecimalMax("100.00") public BigDecimal score;
        @Size(max = 200) public String result;
        @Size(max = 1000) public String applicationMaterial;
        @NotBlank @Size(max = 100) public String subjectOwner;
        @NotNull public Integer version;
    }
    public static class Review {
        @NotBlank @Pattern(regexp = "^(已通过|已驳回|已发布|已归档)$") public String status;
        @Size(max = 500) public String reviewComment;
    }
}
