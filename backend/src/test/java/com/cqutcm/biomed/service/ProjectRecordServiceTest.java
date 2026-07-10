package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.ProjectApplication;
import com.cqutcm.biomed.entity.ProjectMember;
import com.cqutcm.biomed.entity.ResearchProject;
import com.cqutcm.biomed.mapper.ProjectApplicationMapper;
import com.cqutcm.biomed.mapper.ProjectMemberMapper;
import com.cqutcm.biomed.mapper.ResearchProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectRecordServiceTest {

    @Mock
    private ResearchProjectMapper projectMapper;
    @Mock
    private ProjectApplicationMapper applicationMapper;
    @Mock
    private ProjectMemberMapper memberMapper;

    private ProjectRecordService service;

    @BeforeEach
    void setUp() {
        service = new ProjectRecordService(projectMapper, applicationMapper, memberMapper, new PermissionService());
    }

    @Test
    void studentCannotUseGenericProjectUpdate() {
        when(projectMapper.findById("project-1")).thenReturn(project("project-1", "李老师", "已发布"));

        assertThrows(AuthorizationDeniedException.class, () -> service.update(Map.of(
                "id", "project-1",
                "title", "被篡改的课题",
                "status", "已发布",
                "_actorName", "当前学生",
                "_actorRole", "student"
        )));
        verify(projectMapper, never()).updateMap(org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void nonOwnerCannotEditProject() {
        when(projectMapper.findById("project-1")).thenReturn(project("project-1", "李老师", "已发布"));

        assertThrows(AuthorizationDeniedException.class, () -> service.update(Map.of(
                "id", "project-1",
                "title", "其他教师修改",
                "_actorName", "王老师",
                "_actorRole", "teacher"
        )));
    }

    @Test
    void ownerCannotChangeLeaderOrReviewStatus() {
        ResearchProject existing = project("project-1", "李老师", "草稿");
        existing.setApplicantRequests("学生A");
        when(projectMapper.findById("project-1")).thenReturn(existing);
        when(projectMapper.updateMap(org.mockito.ArgumentMatchers.anyMap())).thenReturn(1);
        ArgumentCaptor<Map<String, Object>> captor = mapCaptor();

        service.update(Map.of(
                "id", "project-1",
                "title", "负责人正常编辑",
                "leader", "被替换负责人",
                "status", "已驳回",
                "applicantRequests", "被篡改申请人",
                "_actorName", "李老师",
                "_actorRole", "teacher"
        ));

        verify(projectMapper).updateMap(captor.capture());
        assertEquals("李老师", captor.getValue().get("leader"));
        assertEquals("草稿", captor.getValue().get("status"));
        assertEquals("学生A", captor.getValue().get("applicantRequests"));
    }

    @Test
    void applicationAlwaysUsesAuthenticatedStudentIdentity() {
        when(projectMapper.findById("project-1")).thenReturn(project("project-1", "李老师", "已发布"));
        when(memberMapper.findByProjectId("project-1")).thenReturn(List.of());
        when(applicationMapper.findByProjectId("project-1")).thenReturn(List.of());
        ArgumentCaptor<ProjectApplication> captor = ArgumentCaptor.forClass(ProjectApplication.class);

        service.apply("project-1", new PermissionService.Actor("当前学生", "student"),
                Map.of("studentName", "伪造学生", "applyReason", "参与数据采集"));

        verify(applicationMapper).insert(captor.capture());
        assertEquals("当前学生", captor.getValue().getStudentName());
        assertEquals("待审批", captor.getValue().getStatus());
    }

    @Test
    void projectOwnerCanApproveAndCreateMember() {
        when(projectMapper.findById("project-1")).thenReturn(project("project-1", "李老师", "已发布"));
        ProjectApplication application = new ProjectApplication();
        application.setId("application-1");
        application.setProjectId("project-1");
        application.setStudentName("当前学生");
        application.setStatus("待审批");
        when(applicationMapper.findById("application-1")).thenReturn(application);
        when(memberMapper.findByProjectId("project-1")).thenReturn(List.of());
        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);

        service.reviewApplication("project-1", "application-1",
                new PermissionService.Actor("李老师", "teacher"), Map.of("action", "approve"));

        verify(applicationMapper).update(application);
        verify(memberMapper).insert(memberCaptor.capture());
        assertEquals("已通过", application.getStatus());
        assertEquals("当前学生", memberCaptor.getValue().getMemberName());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ArgumentCaptor<Map<String, Object>> mapCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(Map.class);
    }

    private ResearchProject project(String id, String leader, String status) {
        ResearchProject project = new ResearchProject();
        project.setId(id);
        project.setTitle("测试课题");
        project.setLeaderName(leader);
        project.setStatus(status);
        project.setRequirements("加入要求");
        project.setStage("研究阶段");
        project.setTransformation("成果转化");
        project.setApplicantRequests("");
        project.setApprovedMembers("");
        project.setRejectedApplicants("");
        project.setVersion(0);
        return project;
    }
}
