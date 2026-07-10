package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class GrowthRecordDTOs {
    public static class Create {
        @NotBlank @Size(max = 100) public String herbName;
        @Size(max = 100) public String district;
        @DecimalMin("-50.00") @DecimalMax("60.00") public BigDecimal temperature;
        @DecimalMin("0.00") @DecimalMax("100.00") public BigDecimal humidity;
        @DecimalMin("0.00") @DecimalMax("14.00") public BigDecimal soilPh;
        @Size(max = 100) public String growthStage;
        @Size(max = 100) public String collectSource;
    }
    public static class Update {
        @NotBlank public String id;
        @NotBlank @Size(max = 100) public String herbName;
        @Size(max = 100) public String district;
        @DecimalMin("-50.00") @DecimalMax("60.00") public BigDecimal temperature;
        @DecimalMin("0.00") @DecimalMax("100.00") public BigDecimal humidity;
        @DecimalMin("0.00") @DecimalMax("14.00") public BigDecimal soilPh;
        @Size(max = 100) public String growthStage;
        @Size(max = 100) public String collectSource;
        @NotNull public Integer version;
    }
}
