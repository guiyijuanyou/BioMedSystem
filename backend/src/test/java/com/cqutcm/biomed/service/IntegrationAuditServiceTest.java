package com.cqutcm.biomed.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationAuditServiceTest {
    @Mock
    private JdbcTemplate jdbc;

    private IntegrationAuditService service;

    @BeforeEach
    void setUp() {
        service = new IntegrationAuditService(jdbc, new PermissionService());
    }

    @Test
    void recordsExternalCallWithoutRawCredentialFields() {
        IntegrationAuditService.AuditEvent event = new IntegrationAuditService.AuditEvent(
                "request-123", "APP", "APP_BATCH_UPLOAD", "collection_device",
                "device-abcdef1234567890", "POST", "/api/mobile/growth-records/batch",
                "127.0.0.1", "digest", 128, 200, true, "HTTP_200", 25);

        service.record(event);

        verify(jdbc).update(anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), any(Long.class),
                any(Integer.class), any(Boolean.class), anyString(), any(Long.class));
    }

    @Test
    void nonAdminCannotReadAuditLog() {
        assertThrows(AuthorizationDeniedException.class, () -> service.list(
                new PermissionService.Actor("teacher", "teacher"), null, null, 100));
    }

    @Test
    void listLimitIsCapped() {
        service.list(new PermissionService.Actor("admin", "admin"), null, null, 9999);

        verify(jdbc).queryForList(anyString(), org.mockito.ArgumentMatchers.eq(500));
    }
}
