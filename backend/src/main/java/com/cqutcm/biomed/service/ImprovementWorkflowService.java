package com.cqutcm.biomed.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ImprovementWorkflowService {
    private final JdbcTemplate jdbc;
    private final PermissionService permissions;
    private final MultiMetricEvaluationService multiMetricService;

    public ImprovementWorkflowService(JdbcTemplate jdbc, PermissionService permissions, MultiMetricEvaluationService multiMetricService) {
        this.jdbc = jdbc;
        this.permissions = permissions;
        this.multiMetricService = multiMetricService;
    }

    public Map<String, Object> overview(PermissionService.Actor actor) {
        if (permissions.isStudent(actor)) {
            throw new AuthorizationDeniedException("students cannot access the improvement workflow");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("issues", jdbc.queryForList("""
                SELECT i.id, i.evaluation_id AS evaluationId, i.source_analysis_id AS sourceAnalysisId,
                       i.batch_id AS batchId, i.title, i.description, i.severity,
                       i.improvement_target AS improvementTarget, i.status,
                       i.recheck_evaluation_id AS recheckEvaluationId, i.creator_name AS creator,
                       i.created_at AS createdAt, e.herb_name AS herbName, e.indicator,
                       e.score AS initialScore, e.result AS initialResult,
                       a.analysis_name AS analysisName, r.score AS recheckScore, r.result AS recheckResult,
                       COUNT(t.id) AS taskCount,
                       SUM(CASE WHEN t.status = 'completed' THEN 1 ELSE 0 END) AS completedTaskCount
                FROM evaluation_issue i
                JOIN evaluation_record e ON e.id = i.evaluation_id
                LEFT JOIN growth_analysis a ON a.id = i.source_analysis_id
                LEFT JOIN evaluation_record r ON r.id = i.recheck_evaluation_id
                LEFT JOIN improvement_training_task t ON t.issue_id = i.id
                GROUP BY i.id, i.evaluation_id, i.source_analysis_id, i.batch_id, i.title, i.description,
                         i.severity, i.improvement_target, i.status, i.recheck_evaluation_id,
                         i.creator_name, i.created_at, e.herb_name, e.indicator, e.score, e.result,
                         a.analysis_name, r.score, r.result
                ORDER BY i.created_at DESC"""));
        result.put("tasks", jdbc.queryForList("""
                SELECT t.id, t.issue_id AS issueId, t.training_material_id AS trainingMaterialId,
                       t.title, t.assignee_name AS assignee, t.due_date AS dueDate,
                       t.completion_note AS completionNote, t.pre_score AS preScore,
                       t.post_score AS postScore, t.status, t.creator_name AS creator,
                       t.completed_at AS completedAt, t.version, m.title AS materialTitle
                FROM improvement_training_task t
                LEFT JOIN training_material m ON m.id = t.training_material_id
                ORDER BY t.created_at DESC"""));
        result.put("evaluations", jdbc.queryForList("SELECT id, batch_id AS batchId, herb_name AS herbName, indicator, score, result, status FROM evaluation_record ORDER BY created_at DESC"));
        result.put("analyses", jdbc.queryForList("SELECT id, batch_id AS batchId, analysis_name AS analysisName, conclusion, status FROM growth_analysis ORDER BY created_at DESC"));
        result.put("materials", jdbc.queryForList("SELECT id, title, status FROM training_material ORDER BY created_at DESC"));
        result.put("achievements", jdbc.queryForList("SELECT id, title, owner_name AS owner, status FROM achievement_record ORDER BY created_at DESC"));
        result.put("evidence", jdbc.queryForList("SELECT e.id,e.achievement_id AS achievementId,e.issue_id AS issueId,e.evidence_title AS evidenceTitle,e.evidence_snapshot AS evidenceSnapshot,q.suggested_points AS suggestedPoints,q.confirmed_points AS confirmedPoints,q.status AS scoringStatus FROM achievement_evidence e LEFT JOIN achievement_quantification q ON q.achievement_evidence_id=e.id ORDER BY e.created_at DESC"));
        return result;
    }

    @Transactional
    public Map<String, Object> createInitialEvaluation(Map<String, Object> body, PermissionService.Actor actor) {
        assertProfessional(actor);
        String analysisId = text(body, "sourceAnalysisId", false);
        String batchId = text(body, "batchId", false);
        String herbName = text(body, "herbName", false);
        if (!analysisId.isBlank()) {
            Map<String, Object> analysis = requiredRow("SELECT batch_id, herb_name, indicator FROM growth_analysis WHERE id = ?", analysisId);
            if (batchId.isBlank() && analysis.get("batch_id") != null) batchId = String.valueOf(analysis.get("batch_id"));
            if (herbName.isBlank() && analysis.get("herb_name") != null) herbName = String.valueOf(analysis.get("herb_name"));
        }
        if (!batchId.isBlank()) {
            Map<String, Object> batch = requiredRow("SELECT h.name AS herb_name FROM herb_batch b JOIN herb h ON h.id=b.herb_id WHERE b.id=?", batchId);
            if (herbName.isBlank()) herbName = String.valueOf(batch.get("herb_name"));
        }
        if (herbName.isBlank()) throw new IllegalArgumentException("herbName or a linked analysis/batch is required");
        String id = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO evaluation_record (id,batch_id,herb_name,indicator,score,result,application_material,subject_owner_name,evaluator_name,evaluator_role,status,version,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,0,NOW())",
                id, blankToNull(batchId), herbName, text(body, "indicator", true), decimal(body.get("score")),
                text(body, "result", true), analysisId.isBlank() ? "Created in improvement workflow" : "Based on analysis " + analysisId,
                text(body, "subjectOwner", false), actor.name(), actor.role(), "草稿");
        return requiredRow("SELECT id, batch_id AS batchId, herb_name AS herbName, indicator, score, result, status FROM evaluation_record WHERE id=?", id);
    }

    @Transactional
    public Map<String, Object> createIssue(String evaluationId, Map<String, Object> body, PermissionService.Actor actor) {
        assertProfessional(actor);
        Map<String, Object> evaluation = requiredRow("SELECT id, batch_id FROM evaluation_record WHERE id = ?", evaluationId);
        String analysisId = text(body, "sourceAnalysisId", false);
        if (!analysisId.isBlank()) requiredRow("SELECT id FROM growth_analysis WHERE id = ?", analysisId);
        String id = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO evaluation_issue (id,evaluation_id,source_analysis_id,batch_id,title,description,severity,improvement_target,creator_name) VALUES (?,?,?,?,?,?,?,?,?)",
                id, evaluationId, blankToNull(analysisId), evaluation.get("batch_id"), text(body, "title", true),
                text(body, "description", false), allowed(text(body, "severity", false), List.of("low", "medium", "high"), "medium"),
                text(body, "improvementTarget", false), actor.name());
        return requiredRow("SELECT * FROM evaluation_issue WHERE id = ?", id);
    }

    @Transactional
    public Map<String, Object> createTask(String issueId, Map<String, Object> body, PermissionService.Actor actor) {
        assertProfessional(actor);
        requiredRow("SELECT id FROM evaluation_issue WHERE id = ?", issueId);
        String materialId = text(body, "trainingMaterialId", false);
        if (!materialId.isBlank()) requiredRow("SELECT id FROM training_material WHERE id = ?", materialId);
        String id = UUID.randomUUID().toString();
        String due = text(body, "dueDate", false);
        jdbc.update("INSERT INTO improvement_training_task (id,issue_id,training_material_id,title,assignee_name,due_date,pre_score,creator_name) VALUES (?,?,?,?,?,?,?,?)",
                id, issueId, blankToNull(materialId), text(body, "title", true), text(body, "assignee", true),
                due.isBlank() ? null : Date.valueOf(due), decimal(body.get("preScore")), actor.name());
        jdbc.update("UPDATE evaluation_issue SET status='training', updated_at=NOW(), version=version+1 WHERE id=? AND status='open'", issueId);
        return requiredRow("SELECT * FROM improvement_training_task WHERE id = ?", id);
    }

    @Transactional
    public Map<String, Object> completeTask(String taskId, Map<String, Object> body, PermissionService.Actor actor) {
        assertProfessional(actor);
        Map<String, Object> task = requiredRow("SELECT * FROM improvement_training_task WHERE id = ?", taskId);
        if (!permissions.isAdmin(actor) && !actor.name().equals(String.valueOf(task.get("assignee_name")))
                && !actor.name().equals(String.valueOf(task.get("creator_name")))) {
            throw new AuthorizationDeniedException("only the assignee, creator or admin can complete this task");
        }
        jdbc.update("UPDATE improvement_training_task SET status='completed', completion_note=?, post_score=?, completed_at=NOW(), updated_at=NOW(), version=version+1 WHERE id=?",
                text(body, "completionNote", true), decimal(body.get("postScore")), taskId);
        return requiredRow("SELECT * FROM improvement_training_task WHERE id = ?", taskId);
    }

    @Transactional
    public Map<String, Object> createRecheck(String issueId, Map<String, Object> body, PermissionService.Actor actor) {
        assertProfessional(actor);
        Map<String, Object> issue = requiredRow("SELECT * FROM evaluation_issue WHERE id = ?", issueId);
        Integer pending = jdbc.queryForObject("SELECT COUNT(*) FROM improvement_training_task WHERE issue_id=? AND status<>'completed'", Integer.class, issueId);
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM improvement_training_task WHERE issue_id=?", Integer.class, issueId);
        if (total == null || total == 0 || (pending != null && pending > 0)) {
            throw new IllegalArgumentException("all training tasks must be completed before re-evaluation");
        }
        Map<String, Object> original = requiredRow("SELECT * FROM evaluation_record WHERE id = ?", issue.get("evaluation_id"));
        String id = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO evaluation_record (id,batch_id,herb_name,indicator,score,result,application_material,subject_owner_name,evaluator_name,evaluator_role,status,version,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,0,NOW())",
                id, original.get("batch_id"), original.get("herb_name"), original.get("indicator"), decimal(body.get("score")),
                text(body, "result", true), "Re-evaluation for issue " + issueId, original.get("subject_owner_name"), actor.name(), actor.role(), "草稿");
        jdbc.update("UPDATE evaluation_issue SET recheck_evaluation_id=?, status='rechecking', updated_at=NOW(), version=version+1 WHERE id=?", id, issueId);
        return requiredRow("SELECT * FROM evaluation_record WHERE id = ?", id);
    }

    @Transactional
    public Map<String,Object> autoRecheck(String issueId, PermissionService.Actor actor) {
        assertProfessional(actor);
        Map<String,Object> issue=requiredRow("SELECT * FROM evaluation_issue WHERE id=?",issueId);
        Integer pending=jdbc.queryForObject("SELECT COUNT(*) FROM improvement_training_task WHERE issue_id=? AND status<>'completed'",Integer.class,issueId);
        if(pending!=null&&pending>0)throw new IllegalArgumentException("all training tasks must be completed before re-evaluation");
        Map<String,Object> initial=requiredRow("SELECT id,scheme_id,batch_id,total_score FROM multi_metric_evaluation WHERE evaluation_record_id=?",issue.get("evaluation_id"));
        Map<String,Object> result=multiMetricService.evaluate(String.valueOf(initial.get("scheme_id")),String.valueOf(initial.get("batch_id")),actor);
        Map<String,Object> evaluation=(Map<String,Object>)result.get("evaluation");
        jdbc.update("UPDATE evaluation_issue SET recheck_evaluation_id=?,status='rechecking',updated_at=NOW(),version=version+1 WHERE id=?",evaluation.get("evaluation_record_id"),issueId);
        return Map.of("initialScore",initial.get("total_score"),"recheckScore",evaluation.get("total_score"),"improvement",new BigDecimal(String.valueOf(evaluation.get("total_score"))).subtract(new BigDecimal(String.valueOf(initial.get("total_score")))),"evaluation",evaluation);
    }

    @Transactional
    public void closeIssue(String issueId, PermissionService.Actor actor) {
        if (!permissions.isAdmin(actor)) throw new AuthorizationDeniedException("only admin can close an improvement issue");
        Map<String, Object> issue = requiredRow("SELECT recheck_evaluation_id FROM evaluation_issue WHERE id = ?", issueId);
        if (issue.get("recheck_evaluation_id") == null) throw new IllegalArgumentException("re-evaluation is required before closure");
        jdbc.update("UPDATE evaluation_issue SET status='closed', closed_by=?, closed_at=NOW(), updated_at=NOW(), version=version+1 WHERE id=?", actor.name(), issueId);
    }

    @Transactional
    public Map<String, Object> addEvidence(String achievementId, Map<String, Object> body, PermissionService.Actor actor) {
        assertProfessional(actor);
        Map<String, Object> achievement = requiredRow("SELECT owner_name FROM achievement_record WHERE id = ?", achievementId);
        if (!permissions.isAdmin(actor) && !actor.name().equals(String.valueOf(achievement.get("owner_name")))) {
            throw new AuthorizationDeniedException("only the achievement owner or admin can attach evidence");
        }
        String issueId = text(body, "issueId", true);
        Map<String, Object> issue = requiredRow("SELECT * FROM evaluation_issue WHERE id = ?", issueId);
        if (!"closed".equals(issue.get("status"))) throw new IllegalArgumentException("only closed improvements can become achievement evidence");
        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT id FROM achievement_evidence WHERE achievement_id=? AND source_type='improvement_issue' AND source_id=?",
                achievementId, issueId);
        if (!existing.isEmpty()) {
            return evidenceResult(String.valueOf(existing.get(0).get("id")), true);
        }
        String id = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO achievement_evidence (id,achievement_id,issue_id,source_type,source_id,evidence_title,evidence_snapshot,creator_name) VALUES (?,?,?,?,?,?,?,?)",
                id, achievementId, issueId, "improvement_issue", issueId, text(body, "evidenceTitle", true), text(body, "evidenceSnapshot", false), actor.name());
        Map<String,Object> scores=requiredRow("SELECT a.score AS initial_score,r.score AS recheck_score FROM evaluation_issue i JOIN evaluation_record a ON a.id=i.evaluation_id JOIN evaluation_record r ON r.id=i.recheck_evaluation_id WHERE i.id=?",issueId);
        Map<String,Object> rule=requiredRow("SELECT * FROM achievement_scoring_rule WHERE rule_code='QUALITY_IMPROVEMENT' AND status='active' ORDER BY version_no DESC LIMIT 1");
        BigDecimal initialScore=decimal(scores.get("initial_score")),recheckScore=decimal(scores.get("recheck_score")),delta=recheckScore.subtract(initialScore).max(BigDecimal.ZERO),base=decimal(rule.get("base_points")),suggested=base.add(delta.multiply(decimal(rule.get("improvement_factor")))).min(decimal(rule.get("max_points")));
        jdbc.update("INSERT INTO achievement_quantification(id,achievement_evidence_id,issue_id,rule_id,rule_version,initial_score,recheck_score,improvement_value,base_points,suggested_points) VALUES(?,?,?,?,?,?,?,?,?,?)",UUID.randomUUID().toString(),id,issueId,rule.get("id"),rule.get("version_no"),initialScore,recheckScore,delta,base,suggested);
        return evidenceResult(id, false);
    }

    private Map<String, Object> evidenceResult(String evidenceId, boolean alreadyLinked) {
        Map<String, Object> result = new LinkedHashMap<>(requiredRow("""
                SELECT e.id, e.achievement_id AS achievementId, e.issue_id AS issueId,
                       e.evidence_title AS evidenceTitle, e.evidence_snapshot AS evidenceSnapshot,
                       q.suggested_points AS suggestedPoints, q.confirmed_points AS confirmedPoints
                FROM achievement_evidence e
                LEFT JOIN achievement_quantification q ON q.achievement_evidence_id=e.id
                WHERE e.id=?
                """, evidenceId));
        result.put("alreadyLinked", alreadyLinked);
        return result;
    }

    @Transactional public void confirmPoints(String evidenceId,BigDecimal points,PermissionService.Actor actor){if(!permissions.isAdmin(actor))throw new AuthorizationDeniedException("only admin can confirm achievement points");jdbc.update("UPDATE achievement_quantification SET confirmed_points=?,status='confirmed',confirmed_by=?,confirmed_at=NOW() WHERE achievement_evidence_id=?",points,actor.name(),evidenceId);}

    private void assertProfessional(PermissionService.Actor actor) {
        if (permissions.isStudent(actor)) throw new AuthorizationDeniedException("students cannot maintain the improvement workflow");
    }

    private Map<String, Object> requiredRow(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
        if (rows.isEmpty()) throw new IllegalArgumentException("referenced record not found");
        return rows.get(0);
    }

    private String text(Map<String, Object> body, String key, boolean required) {
        String value = String.valueOf(body.getOrDefault(key, "")).trim();
        if (required && value.isBlank()) throw new IllegalArgumentException(key + " is required");
        return value;
    }

    private Object blankToNull(String value) { return value.isBlank() ? null : value; }
    private String allowed(String value, List<String> values, String fallback) { return values.contains(value) ? value : fallback; }
    private BigDecimal decimal(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        return new BigDecimal(String.valueOf(value));
    }
}
