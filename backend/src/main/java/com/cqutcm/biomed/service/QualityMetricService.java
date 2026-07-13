package com.cqutcm.biomed.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class QualityMetricService {
    private final JdbcTemplate jdbc;
    private final PermissionService permissions;

    public QualityMetricService(JdbcTemplate jdbc, PermissionService permissions) {
        this.jdbc = jdbc;
        this.permissions = permissions;
    }

    public Map<String, Object> overview(PermissionService.Actor actor) {
        if (permissions.isStudent(actor)) throw new AuthorizationDeniedException("students cannot manage quality metrics");
        return Map.of(
                "definitions", jdbc.queryForList("SELECT id,metric_code AS metricCode,metric_name AS metricName,herb_id AS herbId,source_type AS sourceType,source_field AS sourceField,unit_name AS unitName,minimum_value AS minimumValue,maximum_value AS maximumValue,target_value AS targetValue,weight_value AS weightValue,version_no AS versionNo,status FROM quality_metric_definition ORDER BY metric_name,version_no DESC"),
                "batches", jdbc.queryForList("SELECT b.id,b.batch_code AS batchCode,b.batch_name AS batchName,h.name AS herbName FROM herb_batch b JOIN herb h ON h.id=b.herb_id WHERE b.status='active' ORDER BY b.created_at DESC"),
                "results", jdbc.queryForList("SELECT id,batch_id AS batchId,metric_name AS metricName,measured_value AS measuredValue,unit_name AS unitName,judgement,score,source_count AS sourceCount,calculation_note AS calculationNote,calculated_at AS calculatedAt FROM batch_metric_result ORDER BY calculated_at DESC LIMIT 200")
        );
    }

    @Transactional
    public Map<String, Object> createDefinition(Map<String, Object> body, PermissionService.Actor actor) {
        if (!permissions.isAdmin(actor)) throw new AuthorizationDeniedException("only admin can maintain metric standards");
        String code = required(body, "metricCode").toUpperCase(Locale.ROOT);
        Integer nextVersion = jdbc.queryForObject("SELECT COALESCE(MAX(version_no),0)+1 FROM quality_metric_definition WHERE metric_code=?", Integer.class, code);
        String id = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO quality_metric_definition (id,metric_code,metric_name,herb_id,source_type,source_field,unit_name,minimum_value,maximum_value,target_value,weight_value,version_no,status,created_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                id, code, required(body,"metricName"), nullable(body,"herbId"), required(body,"sourceType"), required(body,"sourceField"), nullable(body,"unitName"), decimal(body,"minimumValue"), decimal(body,"maximumValue"), decimal(body,"targetValue"), decimal(body,"weightValue"), nextVersion, "active", actor.name());
        return jdbc.queryForMap("SELECT * FROM quality_metric_definition WHERE id=?", id);
    }

    @Transactional
    public List<Map<String, Object>> calculate(String batchId, PermissionService.Actor actor) {
        if (permissions.isStudent(actor)) throw new AuthorizationDeniedException("students cannot calculate quality metrics");
        Map<String,Object> batch = requiredRow("SELECT herb_id FROM herb_batch WHERE id=?", batchId);
        List<Map<String,Object>> definitions = jdbc.queryForList("SELECT * FROM quality_metric_definition WHERE status='active' AND (herb_id IS NULL OR herb_id=?) ORDER BY metric_name", batch.get("herb_id"));
        jdbc.update("DELETE FROM batch_metric_result WHERE batch_id=?", batchId);
        List<Map<String,Object>> output = new ArrayList<>();
        for (Map<String,Object> definition : definitions) {
            Measurement m = measure(batchId, String.valueOf(definition.get("source_field")));
            String judgement = judge(m.value(), number(definition.get("minimum_value")), number(definition.get("maximum_value")));
            BigDecimal score = score(m.value(), number(definition.get("minimum_value")), number(definition.get("maximum_value")), number(definition.get("target_value")));
            String id = UUID.randomUUID().toString();
            jdbc.update("INSERT INTO batch_metric_result (id,batch_id,metric_definition_id,metric_code,metric_name,measured_value,unit_name,judgement,score,source_count,calculation_note,calculated_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    id,batchId,definition.get("id"),definition.get("metric_code"),definition.get("metric_name"),m.value(),definition.get("unit_name"),judgement,score,m.count(),m.note(),actor.name());
            output.add(jdbc.queryForMap("SELECT id,batch_id AS batchId,metric_name AS metricName,measured_value AS measuredValue,unit_name AS unitName,judgement,score,source_count AS sourceCount,calculation_note AS calculationNote FROM batch_metric_result WHERE id=?", id));
        }
        return output;
    }

    private Measurement measure(String batchId, String field) {
        return switch (field) {
            case "temperature_avg" -> aggregate(batchId,"SELECT AVG(temperature),COUNT(temperature) FROM growth_record WHERE batch_id=?","生长记录温度平均值");
            case "humidity_avg" -> aggregate(batchId,"SELECT AVG(humidity),COUNT(humidity) FROM growth_record WHERE batch_id=?","生长记录湿度平均值");
            case "soil_ph_avg" -> aggregate(batchId,"SELECT AVG(soil_ph),COUNT(soil_ph) FROM growth_record WHERE batch_id=?","生长记录土壤pH平均值");
            case "similarity_max" -> aggregate(batchId,"SELECT MAX(similarity),COUNT(similarity) FROM spectrum_comparison WHERE batch_id=?","图谱比对最高相似度");
            default -> new Measurement(null,0,"暂不支持的数据来源字段: " + field);
        };
    }

    private Measurement aggregate(String batchId,String sql,String note) {
        return jdbc.queryForObject(sql, (rs,row) -> new Measurement(rs.getBigDecimal(1)==null?null:rs.getBigDecimal(1).setScale(4,RoundingMode.HALF_UP),rs.getInt(2),note), batchId);
    }
    private String judge(BigDecimal value,BigDecimal min,BigDecimal max) {
        if(value==null)return "no_data";
        if(min!=null&&value.compareTo(min)<0)return "below_standard";
        if(max!=null&&value.compareTo(max)>0)return "above_standard";
        return "qualified";
    }
    private BigDecimal score(BigDecimal value, BigDecimal min, BigDecimal max, BigDecimal target) {
        if (value == null) return null;
        if (target == null) target = min != null && max != null ? min.add(max).divide(BigDecimal.valueOf(2)) : value;
        if (min != null && value.compareTo(min) < 0) return min.signum() == 0 ? BigDecimal.ZERO : value.divide(min,4,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(79)).max(BigDecimal.ZERO).setScale(2,RoundingMode.HALF_UP);
        if (max != null && value.compareTo(max) > 0) { BigDecimal span=max.subtract(target).abs().max(BigDecimal.ONE); return BigDecimal.valueOf(79).subtract(value.subtract(max).divide(span,4,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(30))).max(BigDecimal.ZERO).setScale(2,RoundingMode.HALF_UP); }
        BigDecimal distance=value.compareTo(target)<=0&&min!=null?target.subtract(min):(max==null?BigDecimal.ONE:max.subtract(target));
        if(distance.signum()<=0)return BigDecimal.valueOf(100);
        return BigDecimal.valueOf(100).subtract(value.subtract(target).abs().divide(distance,4,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(20))).max(BigDecimal.valueOf(80)).setScale(2,RoundingMode.HALF_UP);
    }
    private Map<String,Object> requiredRow(String sql,Object... args){List<Map<String,Object>> rows=jdbc.queryForList(sql,args);if(rows.isEmpty())throw new IllegalArgumentException("record not found");return rows.get(0);}
    private String required(Map<String,Object>b,String k){String v=String.valueOf(b.getOrDefault(k,"")).trim();if(v.isBlank())throw new IllegalArgumentException(k+" is required");return v;}
    private Object nullable(Map<String,Object>b,String k){String v=String.valueOf(b.getOrDefault(k,"")).trim();return v.isBlank()?null:v;}
    private BigDecimal decimal(Map<String,Object>b,String k){Object v=b.get(k);return v==null||String.valueOf(v).isBlank()?null:new BigDecimal(String.valueOf(v));}
    private BigDecimal number(Object v){return v==null?null:new BigDecimal(String.valueOf(v));}
    private record Measurement(BigDecimal value,int count,String note){}
}
