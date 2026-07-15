package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.AchievementRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AchievementRecordMapper extends BiomedBaseMapper<AchievementRecord> {
    @Select("""
            SELECT a.id, a.title, a.owner_name AS owner, a.source_module AS sourceModule,
                   a.course_id AS courseId, a.course_title AS courseTitle,
                   a.batch_id AS batchId, a.herb_name AS herbName,
                   a.project_title AS projectTitle, a.standard_id AS standardId,
                   a.category, a.level_name AS level, a.score, a.evidence,
                   a.evidence_file_ids AS evidenceFileIds, a.status,
                   a.reviewer_name AS reviewerName, a.review_comment AS reviewComment,
                   a.reviewed_at AS reviewedAt, a.version,
                   a.created_at AS createdAt, a.updated_at AS updatedAt,
                   (SELECT COUNT(*) FROM achievement_evidence ae
                    WHERE ae.achievement_id = a.id) AS linkedEvidenceCount,
                   (SELECT GROUP_CONCAT(ae.evidence_title ORDER BY ae.created_at DESC SEPARATOR '；')
                    FROM achievement_evidence ae
                    WHERE ae.achievement_id = a.id) AS linkedEvidenceTitles,
                   (SELECT SUM(aq.suggested_points)
                    FROM achievement_evidence ae
                    JOIN achievement_quantification aq ON aq.achievement_evidence_id = ae.id
                    WHERE ae.achievement_id = a.id) AS suggestedPoints,
                   (SELECT SUM(aq.confirmed_points)
                    FROM achievement_evidence ae
                    JOIN achievement_quantification aq ON aq.achievement_evidence_id = ae.id
                    WHERE ae.achievement_id = a.id) AS confirmedPoints
            FROM achievement_record a
            ORDER BY a.created_at DESC
            """)
    List<Map<String, Object>> findAllAsMap();
}
