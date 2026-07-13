package com.cqutcm.biomed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IntegrationAuditService {
    private static final Logger log = LoggerFactory.getLogger(IntegrationAuditService.class);

    private final JdbcTemplate jdbc;
    private final PermissionService permissions;

    public IntegrationAuditService(JdbcTemplate jdbc, PermissionService permissions) {
        this.jdbc = jdbc;
        this.permissions = permissions;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditEvent event) {
        try {
            jdbc.update("""
                    INSERT INTO integration_audit_log(
                        id, request_id, protocol_type, operation_name, caller_type,
                        caller_identifier, http_method, request_path, source_ip,
                        request_digest, request_bytes, http_status, success_flag,
                        result_code, duration_ms
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, UUID.randomUUID().toString(), event.requestId(), event.protocol(),
                    event.operation(), event.callerType(), event.callerIdentifier(),
                    event.method(), event.path(), event.sourceIp(), event.requestDigest(),
                    event.requestBytes(), event.httpStatus(), event.success(),
                    event.resultCode(), event.durationMs());
        } catch (RuntimeException ex) {
            log.error("Failed to persist integration audit event {}", event.requestId(), ex);
        }
    }

    public List<Map<String, Object>> list(
            PermissionService.Actor actor,
            String protocol,
            Boolean success,
            int limit
    ) {
        if (!permissions.isAdmin(actor)) {
            throw new AuthorizationDeniedException("only admin can read integration audit logs");
        }
        int safeLimit = Math.max(1, Math.min(limit, 500));
        String normalizedProtocol = protocol == null ? "" : protocol.trim().toUpperCase();
        if (!normalizedProtocol.isBlank() && success != null) {
            return jdbc.queryForList("""
                    SELECT * FROM integration_audit_log
                     WHERE protocol_type = ? AND success_flag = ?
                     ORDER BY occurred_at DESC LIMIT ?
                    """, normalizedProtocol, success, safeLimit);
        }
        if (!normalizedProtocol.isBlank()) {
            return jdbc.queryForList("""
                    SELECT * FROM integration_audit_log
                     WHERE protocol_type = ?
                     ORDER BY occurred_at DESC LIMIT ?
                    """, normalizedProtocol, safeLimit);
        }
        if (success != null) {
            return jdbc.queryForList("""
                    SELECT * FROM integration_audit_log
                     WHERE success_flag = ?
                     ORDER BY occurred_at DESC LIMIT ?
                    """, success, safeLimit);
        }
        return jdbc.queryForList("""
                SELECT * FROM integration_audit_log
                 ORDER BY occurred_at DESC LIMIT ?
                """, safeLimit);
    }

    public record AuditEvent(
            String requestId,
            String protocol,
            String operation,
            String callerType,
            String callerIdentifier,
            String method,
            String path,
            String sourceIp,
            String requestDigest,
            long requestBytes,
            int httpStatus,
            boolean success,
            String resultCode,
            long durationMs
    ) {}
}
