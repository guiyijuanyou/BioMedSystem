package com.cqutcm.biomed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class GrowthAnalysisComputeService {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public GrowthAnalysisComputeService(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    /** Lightweight batch summary for analysis page — avoids loading full growth records */
    public Map<String, Object> batchSummary(String batchId) {
        Map<String, Object> batch = requiredRow(
                "SELECT b.id, b.batch_code AS batchCode, b.batch_name AS batchName, " +
                "h.name AS herbName, b.district, b.responsible_person AS responsiblePerson " +
                "FROM herb_batch b JOIN herb h ON h.id = b.herb_id WHERE b.id = ?", batchId);

        // Only aggregate — don't fetch all rows
        List<Map<String, Object>> agg = jdbc.queryForList(
                "SELECT COUNT(*) AS cnt, MIN(recorded_at) AS dateMin, MAX(recorded_at) AS dateMax, " +
                "COUNT(DISTINCT growth_stage) AS stageCnt " +
                "FROM growth_record WHERE batch_id = ?", batchId);

        Map<String, Object> result = new LinkedHashMap<>(batch);
        Map<String, Object> a = agg.get(0);
        result.put("recordCount", ((Number) a.get("cnt")).intValue());
        result.put("dateStart", a.get("dateMin") != null ? a.get("dateMin").toString().substring(0, 10) : "");
        result.put("dateEnd",   a.get("dateMax") != null ? a.get("dateMax").toString().substring(0, 10) : "");
        result.put("stageCount", ((Number) a.get("stageCnt")).intValue());
        return result;
    }

    /**
     * Execute full analysis for a batch.
     * @param batchId   the herb batch ID
     * @param body      { indicators: [{field, label, unit, optMin, optMax}], dateRange: {start, end} }
     * @param actorName analyst name
     * @return full analysis result (not yet saved)
     */
    public Map<String, Object> compute(String batchId, Map<String, Object> body, String actorName) {
        // 1. Load batch info
        Map<String, Object> batch = requiredRow(
                "SELECT b.id, b.batch_code AS batchCode, b.batch_name AS batchName, " +
                "h.name AS herbName, b.district, b.responsible_person AS responsiblePerson " +
                "FROM herb_batch b JOIN herb h ON h.id = b.herb_id WHERE b.id = ?", batchId);

        // 2. Parse indicator configs and date range
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> indicators = (List<Map<String, Object>>) body.get("indicators");
        if (indicators == null || indicators.isEmpty()) {
            // Default indicators
            indicators = List.of(
                    Map.of("field", "temperature", "label", "温度", "unit", "°C", "optMin", 15, "optMax", 25),
                    Map.of("field", "humidity", "label", "湿度", "unit", "%", "optMin", 55, "optMax", 75),
                    Map.of("field", "soil_ph", "label", "土壤pH", "unit", "", "optMin", 6.0, "optMax", 7.0)
            );
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> dateRange = (Map<String, Object>) body.getOrDefault("dateRange", Map.of());
        String dateStart = str(dateRange.get("start"));
        String dateEnd   = str(dateRange.get("end"));

        // 3. Load growth records
        List<Map<String, Object>> records;
        if (!dateStart.isEmpty() && !dateEnd.isEmpty()) {
            records = jdbc.queryForList(
                    "SELECT temperature, humidity, soil_ph, growth_stage, recorded_at " +
                    "FROM growth_record WHERE batch_id = ? AND recorded_at BETWEEN ? AND ? " +
                    "ORDER BY recorded_at ASC", batchId, dateStart, dateEnd);
        } else {
            records = jdbc.queryForList(
                    "SELECT temperature, humidity, soil_ph, growth_stage, recorded_at " +
                    "FROM growth_record WHERE batch_id = ? ORDER BY recorded_at ASC", batchId);
        }

        if (records.isEmpty()) {
            throw new IllegalArgumentException("该批次暂无生长数据记录，无法进行分析");
        }

        // Actual date range from data
        String actualStart = str(records.get(0).get("recorded_at")).substring(0, 10);
        String actualEnd   = str(records.get(records.size() - 1).get("recorded_at")).substring(0, 10);

        // 4. Collect stages
        Set<String> stageSet = new LinkedHashSet<>();
        for (Map<String, Object> r : records) {
            String stage = str(r.get("growth_stage"));
            if (!stage.isEmpty()) stageSet.add(stage);
        }

        // 5. Per-indicator analysis
        List<Map<String, Object>> trendResults = new ArrayList<>();
        List<Map<String, Object>> suitabilityResults = new ArrayList<>();

        for (Map<String, Object> ind : indicators) {
            String field = str(ind.get("field"));
            String label = str(ind.get("label"));
            String unit  = str(ind.get("unit"));
            double optMin = num(ind.get("optMin"));
            double optMax = num(ind.get("optMax"));

            // Extract values
            List<Double> values = new ArrayList<>();
            List<String> stages = new ArrayList<>();
            List<String> dates = new ArrayList<>();
            for (Map<String, Object> r : records) {
                Object v = r.get(field);
                if (v != null) {
                    values.add(((Number) v).doubleValue());
                    stages.add(str(r.get("growth_stage")));
                    dates.add(str(r.get("recorded_at")).substring(0, 10));
                }
            }

            if (values.size() < 2 && values.size() != records.size()) {
                // Some records missing this indicator — still analyze what we have
            }

            int n = values.size();
            if (n == 0) continue;

            // --- Trend analysis (linear regression) ---
            double sx = 0, sy = 0, sxy = 0, sx2 = 0;
            for (int i = 0; i < n; i++) {
                double x = i;
                double y = values.get(i);
                sx += x; sy += y; sxy += x * y; sx2 += x * x;
            }
            double slope = (n * sxy - sx * sy) / (n * sx2 - sx * sx);
            double mean = sy / n;
            double variance = 0;
            for (double v : values) variance += Math.pow(v - mean, 2);
            variance /= n;
            double stddev = Math.sqrt(variance);
            double cv = mean != 0 ? (stddev / Math.abs(mean)) * 100 : 0;
            double firstVal = values.get(0);
            double lastVal = values.get(n - 1);
            double totalChange = lastVal - firstVal;

            String direction;
            if (Math.abs(totalChange) < 0.5 && Math.abs(slope) * n < Math.abs(mean) * 0.05) {
                direction = "平稳";
            } else if (slope > 0.01) {
                direction = "上升";
            } else if (slope < -0.01) {
                direction = "下降";
            } else {
                direction = "平稳";
            }

            Map<String, Object> trend = new LinkedHashMap<>();
            trend.put("indicator", field);
            trend.put("label", label);
            trend.put("unit", unit);
            trend.put("n", n);
            trend.put("slope", round(slope, 4));
            trend.put("mean", round(mean, 1));
            trend.put("stddev", round(stddev, 2));
            trend.put("cv", round(cv, 1));
            trend.put("direction", direction);
            trend.put("rangeStart", round(firstVal, 2));
            trend.put("rangeEnd", round(lastVal, 2));
            // Include actual values and dates for frontend charting
            List<Double> chartValues = new ArrayList<>();
            List<String> chartDates = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                chartValues.add(round(values.get(i), 1));
                chartDates.add(dates.get(i));
            }
            trend.put("values", chartValues);
            trend.put("dates", chartDates);
            trendResults.add(trend);

            // --- Suitability analysis ---
            int ok = 0, lo = 0, hi = 0;
            double totalDev = 0;
            Map<String, int[]> stageCounts = new LinkedHashMap<>(); // {stage: [ok, lo, hi, total]}
            Map<String, Double> stageSums = new LinkedHashMap<>();
            Map<String, Double> stageWorst = new LinkedHashMap<>();

            for (int i = 0; i < n; i++) {
                double v = values.get(i);
                String stage = stages.get(i);
                stageCounts.putIfAbsent(stage, new int[4]);
                stageSums.putIfAbsent(stage, 0.0);
                stageWorst.putIfAbsent(stage, 0.0);
                int[] sc = stageCounts.get(stage);
                sc[3]++;

                if (v >= optMin && v <= optMax) { ok++; sc[0]++; }
                else if (v < optMin) { lo++; sc[1]++; totalDev += (optMin - v); }
                else { hi++; sc[2]++; totalDev += (v - optMax); }

                double dev = v < optMin ? (optMin - v) : (v > optMax ? (v - optMax) : 0);
                if (dev > stageWorst.get(stage)) stageWorst.put(stage, dev);
                stageSums.put(stage, stageSums.get(stage) + v);
            }

            double okRate = n > 0 ? round((double) ok / n * 100, 1) : 0;
            double loRate = n > 0 ? round((double) lo / n * 100, 1) : 0;
            double hiRate = n > 0 ? round((double) hi / n * 100, 1) : 0;
            double avgDev = n > 0 ? round(totalDev / n, 2) : 0;

            // Stage detail
            List<Map<String, Object>> stageDetail = new ArrayList<>();
            for (String stage : stageSet) {
                int[] sc = stageCounts.getOrDefault(stage, new int[4]);
                if (sc[3] == 0) continue;
                Map<String, Object> sd = new LinkedHashMap<>();
                sd.put("stage", stage);
                sd.put("mean", round(stageSums.get(stage) / sc[3], 1));
                sd.put("ok", sc[0]); sd.put("lo", sc[1]); sd.put("hi", sc[2]);
                sd.put("total", sc[3]);
                sd.put("worstDev", round(stageWorst.get(stage), 2));
                sd.put("badRate", sc[3] > 0 ? (int) Math.round((sc[1] + sc[2]) * 100.0 / sc[3]) : 0);
                stageDetail.add(sd);
            }

            Map<String, Object> suitability = new LinkedHashMap<>();
            suitability.put("indicator", field);
            suitability.put("label", label);
            suitability.put("unit", unit);
            suitability.put("optMin", optMin);
            suitability.put("optMax", optMax);
            suitability.put("okRate", okRate);
            suitability.put("loRate", loRate);
            suitability.put("hiRate", hiRate);
            suitability.put("ok", ok); suitability.put("lo", lo); suitability.put("hi", hi);
            suitability.put("avgDeviation", avgDev);
            suitability.put("stageDetail", stageDetail);
            suitabilityResults.add(suitability);
        }

        // 6. Generate conclusion
        String conclusion = buildConclusion(
                str(batch.get("herbName")), str(batch.get("batchName")),
                str(batch.get("district")), actualStart, actualEnd,
                records.size(), stageSet.size(), String.join("、", stageSet),
                trendResults, suitabilityResults);

        // 7. Build result
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batchId", batchId);
        result.put("batchName", str(batch.get("batchName")));
        result.put("batchCode", str(batch.get("batchCode")));
        result.put("herbName", str(batch.get("herbName")));
        result.put("district", str(batch.get("district")));
        result.put("dateStart", actualStart);
        result.put("dateEnd", actualEnd);
        result.put("recordCount", records.size());
        result.put("stageCount", stageSet.size());
        result.put("stages", new ArrayList<>(stageSet));
        result.put("indicators", indicators);
        result.put("trendData", trendResults);
        result.put("suitability", suitabilityResults);
        result.put("conclusion", conclusion);
        result.put("analyzedAt", java.time.LocalDateTime.now().toString());
        result.put("analystName", actorName);

        return result;
    }

    /** Generate human-readable conclusion from analysis data */
    private String buildConclusion(String herbName, String batchName, String district,
                                   String dateStart, String dateEnd, int recordCount,
                                   int stageCount, String stageList,
                                   List<Map<String, Object>> trendResults,
                                   List<Map<String, Object>> suitabilityResults) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s（%s批次，%s产区）在%s至%s期间，共采集%d条生长数据，覆盖%s等%d个生长阶段。\n\n",
                herbName, batchName, district, dateStart, dateEnd, recordCount, stageList, stageCount));

        sb.append("关键指标分析：\n");

        for (int i = 0; i < trendResults.size(); i++) {
            Map<String, Object> t = trendResults.get(i);
            Map<String, Object> s = suitabilityResults.get(i);
            String label = str(t.get("label"));
            String unit  = str(t.get("unit"));
            String dir   = str(t.get("direction"));
            double mean  = num(t.get("mean"));
            double cv    = num(t.get("cv"));
            double startV = num(t.get("rangeStart"));
            double endV  = num(t.get("rangeEnd"));
            double okRate = num(s.get("okRate"));
            int lo = (int) num(s.get("lo"));
            int hi = (int) num(s.get("hi"));

            sb.append(String.format("- %s：均值 %.1f%s，呈%s趋势（%.1f→%.1f%s），变异系数 %.1f%%，适宜度 %.1f%%",
                    label, mean, unit, dir, startV, endV, unit, cv, okRate));

            if (lo + hi > 0) {
                sb.append(String.format("，%d次偏低、%d次偏高", lo, hi));
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> stageDetail = (List<Map<String, Object>>) s.get("stageDetail");
                if (stageDetail != null) {
                    String worstStage = "";
                    int worstBad = 0;
                    for (Map<String, Object> sd : stageDetail) {
                        int badRate = (int) num(sd.get("badRate"));
                        if (badRate > worstBad) { worstBad = badRate; worstStage = str(sd.get("stage")); }
                    }
                    if (!worstStage.isEmpty() && worstBad >= 50) {
                        sb.append(String.format("，偏差集中在%s阶段", worstStage));
                    }
                }
            }

            if (cv > 15) sb.append("，波动偏大");
            if (okRate < 70) sb.append("，建议重点关注");
            sb.append("。\n");
        }

        // ---- Verdict (综合判断) ----
        sb.append("\n综合判断：");
        sb.append(buildVerdict(trendResults, suitabilityResults));

        // ---- Suggestions (改进建议) ----
        sb.append("\n\n改进建议：\n");
        sb.append(buildSuggestions(trendResults, suitabilityResults));

        return sb.toString();
    }

    /** Build rich, context-aware verdict */
    private String buildVerdict(List<Map<String, Object>> trendResults,
                                List<Map<String, Object>> suitabilityResults) {
        double avgCompliance = suitabilityResults.stream()
                .mapToDouble(s -> num(s.get("okRate"))).average().orElse(0);

        // Count indicators by severity
        int goodCount = 0, warnCount = 0, badCount = 0;
        String bestIndicator = "", worstIndicator = "";
        double bestRate = 0, worstRate = 100;
        for (Map<String, Object> s : suitabilityResults) {
            double r = num(s.get("okRate"));
            if (r >= 80) goodCount++;
            else if (r >= 60) warnCount++;
            else badCount++;
            if (r > bestRate) { bestRate = r; bestIndicator = str(s.get("label")); }
            if (r < worstRate) { worstRate = r; worstIndicator = str(s.get("label")); }
        }

        // Also check trend vs direction for risk assessment
        boolean hasWorseningTrend = false;
        for (int i = 0; i < trendResults.size(); i++) {
            Map<String, Object> t = trendResults.get(i);
            Map<String, Object> s = suitabilityResults.get(i);
            String dir = str(t.get("direction"));
            double okRate = num(s.get("okRate"));
            int hi = (int) num(s.get("hi"));
            int lo = (int) num(s.get("lo"));
            // Trend direction is making existing deviation worse
            if (("上升".equals(dir) && hi > lo && okRate < 70) ||
                ("下降".equals(dir) && lo > hi && okRate < 70)) {
                hasWorseningTrend = true;
            }
        }

        if (badCount == 0 && warnCount == 0) {
            return String.format("该批次生长环境总体优良，各项指标均在最优区间附近，%s表现最为突出（达标率%.0f%%），反映出当前种植管理方案科学有效。",
                    bestIndicator, bestRate);
        } else if (badCount == 0 && warnCount <= 1) {
            return String.format("该批次生长条件整体良好，%s等%d项指标表现优秀，%s存在轻微偏差（达标率%.0f%%），整体风险可控。",
                    bestIndicator, goodCount, worstIndicator, worstRate);
        } else if (badCount == 0) {
            String extra = hasWorseningTrend ?
                    "需注意的是，部分指标的趋势方向正在加剧已有偏差，若不加以干预可能在后续阶段恶化。" : "";
            return String.format("该批次生长环境基本适宜，但%d项指标存在一定偏差。其中%s的达标率最低（%.0f%%），是当前主要的改善对象。%s",
                    warnCount, worstIndicator, worstRate, extra);
        } else if (badCount == 1) {
            return String.format("该批次存在%d项指标（%s）严重偏离最优区间，达标率仅%.0f%%，%s可能已成为制约该批次品质的关键环境因子，建议优先排查。",
                    badCount, worstIndicator, worstRate, worstIndicator);
        } else {
            return String.format("该批次多项指标（%d项）偏离最优区间，其中%s表现最差（达标率仅%.0f%%），生长环境存在系统性风险，建议从种植布局和管理措施两方面全面排查。",
                    badCount, worstIndicator, worstRate);
        }
    }

    /** Build rich, context-aware suggestions */
    private String buildSuggestions(List<Map<String, Object>> trendResults,
                                    List<Map<String, Object>> suitabilityResults) {
        StringBuilder sb = new StringBuilder();
        int suggestionIdx = 1;

        for (int i = 0; i < suitabilityResults.size(); i++) {
            Map<String, Object> s = suitabilityResults.get(i);
            Map<String, Object> t = trendResults.get(i);
            String label = str(s.get("label"));
            String field = str(s.get("indicator"));
            String unit  = str(t.get("unit"));
            double okRate = num(s.get("okRate"));
            int lo = (int) num(s.get("lo"));
            int hi = (int) num(s.get("hi"));
            double avgDev = num(s.get("avgDeviation"));
            String dir = str(t.get("direction"));
            double cv = num(t.get("cv"));

            if (okRate >= 80 && cv <= 15) continue; // Good indicator, skip

            sb.append(suggestionIdx++).append(". ");

            // Severity descriptor
            String severity = okRate < 50 ? "严重" : okRate < 70 ? "明显" : "轻微";

            // Find worst stage
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stageDetail = (List<Map<String, Object>>) s.get("stageDetail");
            String worstStage = "";
            int worstBad = 0;
            if (stageDetail != null) {
                for (Map<String, Object> sd : stageDetail) {
                    int br = (int) num(sd.get("badRate"));
                    if (br > worstBad) { worstBad = br; worstStage = str(sd.get("stage")); }
                }
            }

            // Direction description
            String deviationDesc = hi > lo ? "偏高" : "偏低";
            String trendNote = "";
            if (("上升".equals(dir) && hi > lo) || ("下降".equals(dir) && lo > hi)) {
                trendNote = "当前趋势正在加剧这一偏差";
            } else if (("下降".equals(dir) && hi > lo) || ("上升".equals(dir) && lo > hi)) {
                trendNote = "但趋势方向正在向最优区间回归，预计后续会逐步改善";
            }

            // Indicator-specific suggestion
            String specific = generateSpecificSuggestion(field, label, hi > lo, avgDev, worstStage, worstBad, severity, cv, okRate);

            sb.append(String.format("【%s】达标率%.0f%%，%s%s（平均偏离%.1f%s）%s。%s\n",
                    label, okRate, severity, deviationDesc, avgDev, unit,
                    !worstStage.isEmpty() && worstBad >= 50 ? "，集中在" + worstStage + "阶段" : "",
                    specific));

            if (!trendNote.isEmpty()) {
                sb.append("   → ").append(trendNote).append("。\n");
            }
        }

        if (suggestionIdx == 1) {
            sb.append("各指标整体表现良好，当前无需特别干预。建议继续保持现有的种植管理方案，定期采集生长数据以便持续监测。\n");
        }

        // Cross-indicator analysis
        boolean highTempLowHum = false;
        double tempOk = 100, humOk = 100;
        for (Map<String, Object> s : suitabilityResults) {
            if ("temperature".equals(str(s.get("indicator")))) tempOk = num(s.get("okRate"));
            if ("humidity".equals(str(s.get("indicator")))) humOk = num(s.get("okRate"));
        }
        if (tempOk < 70 && humOk < 70) {
            sb.append("\n此外，温度和湿度同时偏离最优区间，可能存在复合影响——高温叠加低湿会加速蒸腾作用，增加植株水分胁迫风险，建议综合评估调控优先级。\n");
        }

        return sb.toString();
    }

    /** Generate indicator-specific actionable suggestion */
    private String generateSpecificSuggestion(String field, String label, boolean tooHigh,
                                              double avgDev, String worstStage, int worstBad,
                                              String severity, double cv, double okRate) {
        if ("temperature".equals(field)) {
            if (tooHigh) {
                if (okRate < 50)
                    return String.format("温度严重偏高，建议尽快采取遮阴网或调整种植季节等措施降低环境温度，尤其关注%s阶段。", worstStage);
                else if (okRate < 70)
                    return String.format("温度偏高，建议在%s阶段增加遮阴措施或适当增加灌溉频率以缓解高温胁迫。", worstStage.isEmpty() ? "关键生长" : worstStage);
                else
                    return "温度略高于最优区间，可适当增加通风或轻微遮阴，监测后续趋势变化。";
            } else {
                if (okRate < 50)
                    return String.format("温度严重偏低，建议检查是否因播种过早导致，可考虑覆膜保温或推迟播种期，%s阶段尤需关注。", worstStage);
                else if (okRate < 70)
                    return String.format("温度偏低，%s阶段可考虑覆膜或搭建简易温棚以提高积温。", worstStage.isEmpty() ? "生长前期" : worstStage);
                else
                    return "温度略低于最优区间，若仅出现在生长初期则属正常现象，持续监测即可。";
            }
        }

        if ("humidity".equals(field)) {
            if (tooHigh) {
                if (okRate < 50)
                    return String.format("湿度严重偏高，存在病害风险（如根腐病、叶斑病），建议加强排水和通风，%s阶段尤为关键。", worstStage);
                else if (okRate < 70)
                    return String.format("湿度偏高，建议改善田间通风条件，%s阶段注意排水防涝。", worstStage.isEmpty() ? "关键生长" : worstStage);
                else
                    return "湿度轻微偏高，保持现有排水措施，注意雨季防涝即可。";
            } else {
                if (okRate < 50)
                    return String.format("湿度严重偏低，土壤干旱可能抑制养分吸收和有效成分积累，建议立即增加灌溉频次，%s阶段尤其不能缺水。", worstStage);
                else if (okRate < 70)
                    return String.format("湿度偏低，建议在%s阶段增加灌溉量或覆盖保墒，减少地表蒸发。", worstStage.isEmpty() ? "生长旺盛期" : worstStage);
                else
                    return "湿度略低于最优水平，适当增加灌溉频率，尤其在干旱季节注意补水。";
            }
        }

        if ("soil_ph".equals(field)) {
            if (tooHigh) {
                if (okRate < 50)
                    return String.format("土壤pH严重偏高（偏碱），可能导致铁、锰、锌等微量元素有效性降低，建议施用硫磺粉或有机肥逐步调酸，改良周期约需1-2季。");
                else if (okRate < 70)
                    return String.format("土壤pH偏高，建议增施有机肥或使用生理酸性肥料，逐步将pH调整至适宜范围。");
                else
                    return "土壤pH轻微偏高，可通过增施腐熟有机肥逐步改善，短期影响有限。";
            } else {
                if (okRate < 50)
                    return String.format("土壤pH严重偏低（偏酸），可能抑制根系发育和磷素吸收，建议按50-100kg/亩施用石灰，翻耕后2-3周复测。");
                else if (okRate < 70)
                    return String.format("土壤pH偏低，建议适量施用石灰或草木灰进行改良，分次施用避免剧烈波动。");
                else
                    return "土壤pH轻微偏低，可在下一季整地时适量施用石灰或白云石粉调酸。";
            }
        }

        // Generic fallback
        return tooHigh ?
                String.format("建议排查导致%s偏高的原因，在%s阶段采取相应的调控措施。", label, worstStage.isEmpty() ? "关键时期" : worstStage) :
                String.format("建议排查导致%s偏低的原因，在%s阶段采取相应的补充或改良措施。", label, worstStage.isEmpty() ? "关键时期" : worstStage);
    }

    // ── helpers ──

    private Map<String, Object> requiredRow(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        if (rows.isEmpty()) throw new IllegalArgumentException("record not found");
        return rows.get(0);
    }

    private String str(Object v) { return v == null ? "" : String.valueOf(v); }
    private double num(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return 0; }
    }
    private double round(double v, int places) {
        return new BigDecimal(v).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
