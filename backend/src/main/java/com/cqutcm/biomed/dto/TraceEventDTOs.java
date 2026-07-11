package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class TraceEventDTOs {
    public static class Create {
        public String batchId;
        @NotBlank @Size(max = 100) public String herbName;
        @NotBlank @Size(max = 100) public String traceCode;
        @NotBlank @Size(max = 80) public String eventType;
        @Size(max = 2000) public String eventContent;
        @Size(max = 100) public String operatorName;
        public LocalDateTime eventTime;
        @Size(max = 200) public String location;
    }
    public static class Update {
        @NotBlank public String id;
        public String batchId;
        @NotBlank @Size(max = 100) public String herbName;
        @NotBlank @Size(max = 100) public String traceCode;
        @NotBlank @Size(max = 80) public String eventType;
        @Size(max = 2000) public String eventContent;
        @Size(max = 100) public String operatorName;
        public LocalDateTime eventTime;
        @Size(max = 200) public String location;
        @NotNull public Integer version;
    }
}
