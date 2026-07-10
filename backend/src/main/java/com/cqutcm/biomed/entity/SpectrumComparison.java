package com.cqutcm.biomed.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SpectrumComparison {
    private String id;
    private String herbName;
    private String sampleCode;
    private String district;
    private String spectrumType;
    private String referenceName;
    private BigDecimal similarity;
    private String result;
    private String operatorName;
    private LocalDateTime comparedAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHerbName() { return herbName; }
    public void setHerbName(String herbName) { this.herbName = herbName; }
    public String getSampleCode() { return sampleCode; }
    public void setSampleCode(String sampleCode) { this.sampleCode = sampleCode; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getSpectrumType() { return spectrumType; }
    public void setSpectrumType(String spectrumType) { this.spectrumType = spectrumType; }
    public String getReferenceName() { return referenceName; }
    public void setReferenceName(String referenceName) { this.referenceName = referenceName; }
    public BigDecimal getSimilarity() { return similarity; }
    public void setSimilarity(BigDecimal similarity) { this.similarity = similarity; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public LocalDateTime getComparedAt() { return comparedAt; }
    public void setComparedAt(LocalDateTime comparedAt) { this.comparedAt = comparedAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
