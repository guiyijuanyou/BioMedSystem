package com.cqutcm.biomed.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;

@TableName("herb_encyclopedia")
public class HerbEncyclopedia {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    @TableField("name")
    private String name;
    @TableField("pinyin")
    private String pinyin;
    @TableField("english_name")
    private String englishName;
    @TableField("latin_name")
    private String latinName;
    @TableField("category")
    private String category;
    @TableField("source_desc")
    private String sourceDesc;
    @TableField("origin_desc")
    private String originDesc;
    @TableField("macroscopic")
    private String macroscopic;
    @TableField("quality_desc")
    private String qualityDesc;
    @TableField("nature_flavor")
    private String natureFlavor;
    @TableField("efficacy")
    private String efficacy;
    @TableField("image_file_id")
    private String imageFileId;
    @TableField("source_url")
    private String sourceUrl;
    @Version
    @TableField("version")
    private Integer version;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(value = "updated_at", fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPinyin() { return pinyin; }
    public void setPinyin(String pinyin) { this.pinyin = pinyin; }
    public String getEnglishName() { return englishName; }
    public void setEnglishName(String englishName) { this.englishName = englishName; }
    public String getLatinName() { return latinName; }
    public void setLatinName(String latinName) { this.latinName = latinName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSourceDesc() { return sourceDesc; }
    public void setSourceDesc(String sourceDesc) { this.sourceDesc = sourceDesc; }
    public String getOriginDesc() { return originDesc; }
    public void setOriginDesc(String originDesc) { this.originDesc = originDesc; }
    public String getMacroscopic() { return macroscopic; }
    public void setMacroscopic(String macroscopic) { this.macroscopic = macroscopic; }
    public String getQualityDesc() { return qualityDesc; }
    public void setQualityDesc(String qualityDesc) { this.qualityDesc = qualityDesc; }
    public String getNatureFlavor() { return natureFlavor; }
    public void setNatureFlavor(String natureFlavor) { this.natureFlavor = natureFlavor; }
    public String getEfficacy() { return efficacy; }
    public void setEfficacy(String efficacy) { this.efficacy = efficacy; }
    public String getImageFileId() { return imageFileId; }
    public void setImageFileId(String imageFileId) { this.imageFileId = imageFileId; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
