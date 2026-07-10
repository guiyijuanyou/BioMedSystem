package com.cqutcm.biomed.dto;

import jakarta.validation.constraints.*;

public class TraceEventDTOs {
    public static class Create {
        @NotBlank @Size(max = 100) public String herbName;
        @NotBlank @Size(max = 100) public String traceCode;
        @NotBlank @Size(max = 80) public String eventType;
        @Size(max = 2000) public String eventContent;
        @Size(max = 200) public String location;
    }
    public static class Update {
        @NotBlank public String id;
        @NotBlank @Size(max = 100) public String herbName;
        @NotBlank @Size(max = 100) public String traceCode;
        @NotBlank @Size(max = 80) public String eventType;
        @Size(max = 2000) public String eventContent;
        @Size(max = 200) public String location;
        @NotNull public Integer version;
    }
}
