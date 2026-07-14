package com.cqutcm.biomed.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssistantService {
    private static final int MAX_HISTORY_MESSAGES = 10;
    private static final int MAX_HISTORY_CONTENT_LENGTH = 2_000;

    private final DeepSeekClient deepSeekClient;
    private final StructuredRecordService structuredService;
    private final GrowthRecordService growthService;
    private final CourseRecordService courseService;
    private final ProjectRecordService projectService;

    public AssistantService(DeepSeekClient deepSeekClient,
                            StructuredRecordService structuredService,
                            GrowthRecordService growthService,
                            CourseRecordService courseService,
                            ProjectRecordService projectService) {
        this.deepSeekClient = deepSeekClient;
        this.structuredService = structuredService;
        this.growthService = growthService;
        this.courseService = courseService;
        this.projectService = projectService;
    }

    public Map<String, Object> chat(String question, List<ChatMessage> history) {
        String text = question == null ? "" : question.trim();
        if (text.isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", buildSystemPrompt()));
        messages.addAll(sanitizeHistory(history));
        messages.add(new ChatMessage("user", text));

        return Map.of(
                "answer", deepSeekClient.chat(messages),
                "model", deepSeekClient.model()
        );
    }

    private List<ChatMessage> sanitizeHistory(List<ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        int start = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        return history.subList(start, history.size()).stream()
                .filter(message -> message != null
                        && ("user".equals(message.role()) || "assistant".equals(message.role()))
                        && message.content() != null && !message.content().isBlank())
                .map(message -> new ChatMessage(
                        message.role(),
                        message.content().substring(0, Math.min(message.content().length(), MAX_HISTORY_CONTENT_LENGTH))))
                .toList();
    }

    private String buildSystemPrompt() {
        return """
                你是“中药材生物医药数字化信息系统”的 AI 助手。请使用简体中文，回答准确、简洁、可操作。
                你可以解释系统功能，也可以根据下面提供的实时业务数据回答问题。
                不要编造未提供的记录；无法从上下文确定时要明确说明。不要声称自己已经执行了修改、删除、审核等操作。
                涉及医疗用途时只提供一般信息，并提醒用户咨询合格的医疗专业人员。

                系统模块：中药材与批次、种植生长采集、资源文件、课程教学、研究项目、评价指标、改进建议、业绩成果、溯源与光谱对比。

                当前业务数据摘要：
                - 中药材品种：%d 个
                - 生长采集记录：%d 条
                - 试验课程：%d 门
                - 研究项目：%d 个
                - 评价记录：%d 条
                - 业绩记录：%d 条

                中药材样本（最多 12 条）：%s
                最近生长采集样本（最多 8 条）：%s
                """.formatted(
                structuredService.count("herbs"),
                growthService.count(),
                courseService.courseCount(),
                projectService.count(),
                structuredService.count("evaluations"),
                structuredService.count("achievements"),
                herbContext(),
                growthContext()
        );
    }

    private String herbContext() {
        String result = structuredService.list("herbs").stream()
                .limit(12)
                .map(item -> String.format("%s（地区：%s，规模：%s，溯源码：%s）",
                        value(item, "name"), value(item, "district"),
                        value(item, "scale"), value(item, "traceCode")))
                .collect(Collectors.joining("；"));
        return result.isBlank() ? "暂无" : result;
    }

    private String growthContext() {
        String result = growthService.list().stream()
                .limit(8)
                .map(item -> String.format("%s（地区：%s，温度：%s，湿度：%s，土壤 pH：%s）",
                        value(item, "herbName"), value(item, "district"), value(item, "temperature"),
                        value(item, "humidity"), value(item, "soilPh")))
                .collect(Collectors.joining("；"));
        return result.isBlank() ? "暂无" : result;
    }

    private String value(Map<String, Object> item, String key) {
        return String.valueOf(item.getOrDefault(key, "-"));
    }

    public record ChatMessage(String role, String content) {
    }
}
