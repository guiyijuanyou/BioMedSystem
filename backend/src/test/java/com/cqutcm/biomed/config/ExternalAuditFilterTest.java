package com.cqutcm.biomed.config;

import com.cqutcm.biomed.service.IntegrationAuditService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExternalAuditFilterTest {
    @Mock
    private IntegrationAuditService auditService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void appUploadUsesPseudonymousDeviceIdentifier() throws Exception {
        ExternalAuditFilter filter = new ExternalAuditFilter(auditService);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/api/mobile/growth-records/batch");
        request.addHeader("X-Device-Token", "raw-secret-device-token");
        request.setContentType("application/json");
        request.setContent("{\"records\":[]}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        IntegrationAuditService.AuditEvent event = capturedEvent();
        assertEquals("APP", event.protocol());
        assertEquals("APP_BATCH_UPLOAD", event.operation());
        assertTrue(event.callerIdentifier().startsWith("device-"));
        assertFalse(event.callerIdentifier().contains("raw-secret-device-token"));
        assertEquals(64, event.requestDigest().length());
        assertTrue(response.getHeader("X-Request-Id").length() >= 8);
    }

    @Test
    void soapFailureIsRecordedWithClientCodeAndStatus() throws Exception {
        ExternalAuditFilter filter = new ExternalAuditFilter(auditService);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/soap/school");
        request.addHeader("X-Integration-Client", "CQUTCM-SCHOOL");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) ->
                ((jakarta.servlet.http.HttpServletResponse) res).setStatus(401);

        filter.doFilter(request, response, chain);

        IntegrationAuditService.AuditEvent event = capturedEvent();
        assertEquals("SOAP", event.protocol());
        assertEquals("CQUTCM-SCHOOL", event.callerIdentifier());
        assertEquals(401, event.httpStatus());
        assertFalse(event.success());
    }

    private IntegrationAuditService.AuditEvent capturedEvent() {
        ArgumentCaptor<IntegrationAuditService.AuditEvent> captor =
                ArgumentCaptor.forClass(IntegrationAuditService.AuditEvent.class);
        verify(auditService).record(captor.capture());
        return captor.getValue();
    }
}
