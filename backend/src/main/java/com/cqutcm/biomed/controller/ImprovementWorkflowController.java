package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.AuthService;
import com.cqutcm.biomed.service.ImprovementWorkflowService;
import com.cqutcm.biomed.service.PermissionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/improvement")
public class ImprovementWorkflowController {
    private final AuthService auth;
    private final ImprovementWorkflowService workflow;

    public ImprovementWorkflowController(AuthService auth, ImprovementWorkflowService workflow) {
        this.auth = auth;
        this.workflow = workflow;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return workflow.overview(auth.requireActor(authorization));
    }

    @PostMapping("/evaluations/{evaluationId}/issues")
    public Map<String, Object> createIssue(@PathVariable String evaluationId, @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return workflow.createIssue(evaluationId, body, auth.requireActor(authorization));
    }

    @PostMapping("/evaluations")
    public Map<String, Object> createInitialEvaluation(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return workflow.createInitialEvaluation(body, auth.requireActor(authorization));
    }

    @PostMapping("/issues/{issueId}/tasks")
    public Map<String, Object> createTask(@PathVariable String issueId, @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return workflow.createTask(issueId, body, auth.requireActor(authorization));
    }

    @PutMapping("/tasks/{taskId}/complete")
    public Map<String, Object> completeTask(@PathVariable String taskId, @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return workflow.completeTask(taskId, body, auth.requireActor(authorization));
    }

    @PostMapping("/issues/{issueId}/recheck")
    public Map<String, Object> createRecheck(@PathVariable String issueId, @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return workflow.createRecheck(issueId, body, auth.requireActor(authorization));
    }
    @PostMapping("/issues/{issueId}/auto-recheck") public Map<String,Object> autoRecheck(@PathVariable String issueId,@RequestHeader(value="Authorization",required=false)String authorization){return workflow.autoRecheck(issueId,auth.requireActor(authorization));}

    @PutMapping("/issues/{issueId}/close")
    public Map<String, Object> closeIssue(@PathVariable String issueId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        workflow.closeIssue(issueId, auth.requireActor(authorization));
        return Map.of("message", "issue closed");
    }

    @PostMapping("/achievements/{achievementId}/evidence")
    public Map<String, Object> addEvidence(@PathVariable String achievementId, @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return workflow.addEvidence(achievementId, body, auth.requireActor(authorization));
    }
    @PutMapping("/evidence/{evidenceId}/confirm-points") public Map<String,Object> confirmPoints(@PathVariable String evidenceId,@RequestBody Map<String,Object>body,@RequestHeader(value="Authorization",required=false)String authorization){workflow.confirmPoints(evidenceId,new java.math.BigDecimal(String.valueOf(body.get("points"))),auth.requireActor(authorization));return Map.of("message","points confirmed");}
}
