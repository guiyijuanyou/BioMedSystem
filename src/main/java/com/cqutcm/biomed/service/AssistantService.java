package com.cqutcm.biomed.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssistantService {
    private final GenericRecordService recordService;

    public AssistantService(GenericRecordService recordService) {
        this.recordService = recordService;
    }

    public Map<String, Object> chat(String question) {
        String text = question == null ? "" : question.trim();
        if (text.isBlank()) {
            return Map.of("answer", "可以问我：有哪些中药材、怎么上传资料、怎么查看生长数据、怎么做备份、系统有哪些模块。");
        }

        String answer;
        if (containsAny(text, "统计", "数量", "总览", "多少")) {
            answer = buildSummaryAnswer();
        } else if (containsAny(text, "药材", "品种", "分布", "地图", "区县")) {
            answer = buildHerbAnswer(text);
        } else if (containsAny(text, "采集", "生长", "温度", "湿度", "PH", "ph")) {
            answer = buildGrowthAnswer();
        } else if (containsAny(text, "上传", "资料", "文件", "下载", "查看")) {
            answer = "资料文件在“资料文件”模块管理。选择资料分类和文件后点击上传；上传后可直接“查看”图片、PDF、视频、文本等浏览器支持的文件，也可以点击“下载”保存到本地。";
        } else if (containsAny(text, "备份", "恢复")) {
            answer = "点击页面右上角“自动备份”即可生成当前数据备份文件，备份会保存在项目的 data 目录中。正式部署时可扩展为定时备份和数据库备份。";
        } else if (containsAny(text, "课程", "教学", "视频")) {
            answer = "试验课程模块用于存储课程名称、教师、学时、资料类型和发布状态；线上视频或课件可以先上传到“资料文件”，再在课程中记录资料类型和说明。";
        } else if (containsAny(text, "评价", "非遗", "申报")) {
            answer = "评价体系模块用于记录药材名称、评价指标、评分、评价结果和申报素材，可为非遗申请、品牌申报、产地证明等工作沉淀材料。";
        } else if (containsAny(text, "业绩", "审核", "认定", "标准")) {
            answer = "业绩管理模块支持录入业绩名称、所属单位、分类、级别和审核状态；认定标准模块可以维护学校现有业绩分类分级规则。";
        } else if (containsAny(text, "手机", "移动端", "app", "APP")) {
            answer = "手机端可以通过同一局域网访问电脑 IP 加 8088 端口，例如 http://电脑IP:8088。手机端支持侧拉菜单、生长数据录入、资料查看和基础管理。";
        } else {
            answer = "我可以帮助你使用系统、解释模块、查询当前样本数据、说明上传下载、备份、评价、业绩审核等流程。你可以换个更具体的问题试试。";
        }

        return Map.of("answer", answer);
    }

    private String buildSummaryAnswer() {
        Map<String, Object> summary = recordService.summary();
        return "当前系统样本数据概况：药材品种 " + summary.get("herbCount")
                + " 个，生长采集记录 " + summary.get("growthRecordCount")
                + " 条，试验课程 " + summary.get("courseCount")
                + " 门，研究课题 " + summary.get("projectCount")
                + " 个，评价记录 " + summary.get("evaluationCount")
                + " 条，业绩记录 " + summary.get("achievementCount") + " 条。";
    }

    private String buildHerbAnswer(String question) {
        List<Map<String, Object>> herbs = recordService.list("herbs");
        List<Map<String, Object>> matched = herbs.stream()
                .filter(item -> question.contains(String.valueOf(item.getOrDefault("name", "")))
                        || question.contains(String.valueOf(item.getOrDefault("district", ""))))
                .toList();
        List<Map<String, Object>> source = matched.isEmpty() ? herbs : matched;
        String rows = source.stream()
                .limit(8)
                .map(item -> item.getOrDefault("name", "") + "：" + item.getOrDefault("district", "")
                        + "，规模 " + item.getOrDefault("scale", "-")
                        + "，溯源码 " + item.getOrDefault("traceCode", "-"))
                .collect(Collectors.joining("；"));
        return rows.isBlank() ? "当前还没有药材分布样本。" : "当前药材分布样本包括：" + rows + "。";
    }

    private String buildGrowthAnswer() {
        List<Map<String, Object>> records = recordService.list("growth-records");
        String rows = records.stream()
                .limit(5)
                .map(item -> item.getOrDefault("herbName", "") + "（" + item.getOrDefault("district", "") + "）：温度 "
                        + item.getOrDefault("temperature", "-") + "，湿度 "
                        + item.getOrDefault("humidity", "-") + "，土壤PH "
                        + item.getOrDefault("soilPh", "-"))
                .collect(Collectors.joining("；"));
        return rows.isBlank() ? "当前还没有生长采集记录。" : "最近的生长采集样本：" + rows + "。";
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
