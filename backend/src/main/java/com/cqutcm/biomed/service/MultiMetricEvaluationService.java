package com.cqutcm.biomed.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*; import java.util.*;

@Service
public class MultiMetricEvaluationService {
    private final JdbcTemplate jdbc; private final PermissionService permissions; private final QualityMetricService metricService;
    public MultiMetricEvaluationService(JdbcTemplate jdbc,PermissionService permissions,QualityMetricService metricService){this.jdbc=jdbc;this.permissions=permissions;this.metricService=metricService;}

    public Map<String,Object> overview(PermissionService.Actor actor){professional(actor);return Map.of(
      "schemes",jdbc.queryForList("SELECT s.id,s.scheme_code AS schemeCode,s.scheme_name AS schemeName,s.evaluation_type AS evaluationType,s.herb_id AS herbId,s.passing_score AS passingScore,s.version_no AS versionNo,s.status,COUNT(i.id) AS itemCount,COALESCE(SUM(i.weight_value),0) AS totalWeight FROM evaluation_scheme s LEFT JOIN evaluation_scheme_item i ON i.scheme_id=s.id GROUP BY s.id,s.scheme_code,s.scheme_name,s.evaluation_type,s.herb_id,s.passing_score,s.version_no,s.status ORDER BY s.created_at DESC"),
      "batches",jdbc.queryForList("SELECT b.id,b.batch_code AS batchCode,b.batch_name AS batchName,h.name AS herbName FROM herb_batch b JOIN herb h ON h.id=b.herb_id WHERE b.status='active' ORDER BY b.created_at DESC"),
      "evaluations",jdbc.queryForList("SELECT e.id,e.scheme_id AS schemeId,s.scheme_name AS schemeName,e.batch_id AS batchId,b.batch_name AS batchName,h.name AS herbName,e.total_score AS totalScore,e.grade_name AS gradeName,e.result,e.conclusion,e.evaluator_name AS evaluator,e.created_at AS createdAt FROM multi_metric_evaluation e JOIN evaluation_scheme s ON s.id=e.scheme_id JOIN herb_batch b ON b.id=e.batch_id JOIN herb h ON h.id=b.herb_id ORDER BY e.created_at DESC LIMIT 100")
    );}

    public Map<String,Object> detail(String id,PermissionService.Actor actor){professional(actor);Map<String,Object> out=new LinkedHashMap<>();out.put("evaluation",required("SELECT e.*,s.scheme_name,b.batch_name,h.name AS herb_name FROM multi_metric_evaluation e JOIN evaluation_scheme s ON s.id=e.scheme_id JOIN herb_batch b ON b.id=e.batch_id JOIN herb h ON h.id=b.herb_id WHERE e.id=?",id));out.put("details",jdbc.queryForList("SELECT metric_name AS metricName,metric_version AS metricVersion,measured_value AS measuredValue,unit_name AS unitName,judgement,raw_score AS rawScore,weight_value AS weightValue,weighted_score AS weightedScore,source_count AS sourceCount FROM multi_metric_evaluation_detail WHERE evaluation_id=? ORDER BY metric_name",id));return out;}

    @Transactional public Map<String,Object> evaluate(String schemeId,String batchId,PermissionService.Actor actor){professional(actor);
      Map<String,Object>s=required("SELECT * FROM evaluation_scheme WHERE id=? AND status='active'",schemeId);
      Map<String,Object>b=required("SELECT b.*,h.name AS herb_name FROM herb_batch b JOIN herb h ON h.id=b.herb_id WHERE b.id=?",batchId);
      if(s.get("herb_id")!=null&&!String.valueOf(s.get("herb_id")).equals(String.valueOf(b.get("herb_id"))))throw new IllegalArgumentException("scheme is not applicable to this herb");
      metricService.calculate(batchId,actor);
      List<Map<String,Object>> items=jdbc.queryForList("SELECT i.*,q.metric_code,q.metric_name,q.version_no FROM evaluation_scheme_item i JOIN quality_metric_definition q ON q.id=i.metric_definition_id WHERE i.scheme_id=?",schemeId);
      if(items.isEmpty())throw new IllegalArgumentException("evaluation scheme has no metric items");
      BigDecimal totalWeight=BigDecimal.ZERO,total=BigDecimal.ZERO; List<Map<String,Object>> snapshots=new ArrayList<>(); boolean veto=false;
      for(Map<String,Object>i:items){List<Map<String,Object>>rs=jdbc.queryForList("SELECT * FROM batch_metric_result WHERE batch_id=? AND metric_definition_id=? ORDER BY calculated_at DESC LIMIT 1",batchId,i.get("metric_definition_id"));if(rs.isEmpty()||rs.get(0).get("measured_value")==null)throw new IllegalArgumentException("missing calculated data for metric: "+i.get("metric_name"));Map<String,Object>r=rs.get(0);BigDecimal raw=decimal(r.get("score")),weight=decimal(i.get("weight_value"));BigDecimal weighted=raw.multiply(weight).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);total=total.add(weighted);totalWeight=totalWeight.add(weight);if(Boolean.TRUE.equals(i.get("veto_flag"))&&raw.compareTo(decimal(i.get("minimum_item_score")))<0)veto=true;i.put("result",r);i.put("raw",raw);i.put("weighted",weighted);snapshots.add(i);}
      if(totalWeight.compareTo(BigDecimal.valueOf(100))!=0)total=total.multiply(BigDecimal.valueOf(100)).divide(totalWeight,2,RoundingMode.HALF_UP);
      BigDecimal passing=decimal(s.get("passing_score"));String result=!veto&&total.compareTo(passing)>=0?"qualified":"unqualified";String grade=total.compareTo(BigDecimal.valueOf(90))>=0?"excellent":total.compareTo(BigDecimal.valueOf(80))>=0?"good":total.compareTo(passing)>=0?"qualified":"unqualified";
      String id=UUID.randomUUID().toString(),recordId=UUID.randomUUID().toString();String conclusion="Automated multi-metric evaluation: "+grade+", score "+total;
      jdbc.update("INSERT INTO evaluation_record (id,batch_id,herb_name,indicator,score,result,application_material,subject_owner_name,evaluator_name,evaluator_role,status,version,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,0,NOW())",recordId,batchId,b.get("herb_name"),s.get("scheme_name"),total,result,"multi_metric_evaluation:"+id,b.get("responsible_person"),actor.name(),actor.role(),"草稿");
      jdbc.update("INSERT INTO multi_metric_evaluation (id,scheme_id,scheme_version,batch_id,evaluation_record_id,total_score,grade_name,result,conclusion,evaluator_name) VALUES (?,?,?,?,?,?,?,?,?,?)",id,schemeId,s.get("version_no"),batchId,recordId,total,grade,result,conclusion,actor.name());
      for(Map<String,Object>i:snapshots){Map<String,Object>r=(Map<String,Object>)i.get("result");jdbc.update("INSERT INTO multi_metric_evaluation_detail (id,evaluation_id,metric_definition_id,metric_code,metric_name,metric_version,measured_value,unit_name,judgement,raw_score,weight_value,weighted_score,source_result_id,source_count) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",UUID.randomUUID().toString(),id,i.get("metric_definition_id"),i.get("metric_code"),i.get("metric_name"),i.get("version_no"),r.get("measured_value"),r.get("unit_name"),r.get("judgement"),i.get("raw"),i.get("weight_value"),i.get("weighted"),r.get("id"),r.get("source_count"));}
      return detail(id,actor);
    }
    @Transactional public void delete(String id,PermissionService.Actor actor){if(!permissions.isAdmin(actor))throw new AuthorizationDeniedException("only admin can delete automatic evaluations");Map<String,Object>e=required("SELECT evaluation_record_id FROM multi_metric_evaluation WHERE id=?",id);jdbc.update("DELETE FROM multi_metric_evaluation_detail WHERE evaluation_id=?",id);jdbc.update("DELETE FROM multi_metric_evaluation WHERE id=?",id);if(e.get("evaluation_record_id")!=null)jdbc.update("DELETE FROM evaluation_record WHERE id=? AND status='草稿'",e.get("evaluation_record_id"));}
    private void professional(PermissionService.Actor a){if(permissions.isStudent(a))throw new AuthorizationDeniedException("students cannot maintain professional evaluations");}
    private Map<String,Object>required(String sql,Object...args){List<Map<String,Object>>r=jdbc.queryForList(sql,args);if(r.isEmpty())throw new IllegalArgumentException("record not found");return r.get(0);}private BigDecimal decimal(Object v){return v==null?BigDecimal.ZERO:new BigDecimal(String.valueOf(v));}
}
