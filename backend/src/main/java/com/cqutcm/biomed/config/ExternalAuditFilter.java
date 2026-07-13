package com.cqutcm.biomed.config;

import com.cqutcm.biomed.service.IntegrationAuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

public class ExternalAuditFilter extends OncePerRequestFilter {
    private static final int REQUEST_CACHE_LIMIT = 1024 * 1024;

    private final IntegrationAuditService auditService;

    public ExternalAuditFilter(IntegrationAuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/api/mobile/")
                || path.startsWith("/api/soap/")
                || path.startsWith("/api/files"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper wrapped = request instanceof ContentCachingRequestWrapper existing
                ? existing
                : new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT);
        String requestId = requestId(request.getHeader("X-Request-Id"));
        response.setHeader("X-Request-Id", requestId);
        long startedAt = System.nanoTime();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            filterChain.doFilter(wrapped, response);
        } finally {
            Authentication current = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                authentication = current;
            }
            auditService.record(event(wrapped, response, requestId, startedAt, authentication));
        }
    }

    private IntegrationAuditService.AuditEvent event(
            ContentCachingRequestWrapper request,
            HttpServletResponse response,
            String requestId,
            long startedAt,
            Authentication authentication
    ) {
        String path = request.getRequestURI();
        String protocol = protocol(path);
        Caller caller = caller(request, protocol, authentication);
        byte[] cached = request.getContentAsByteArray();
        long requestBytes = request.getContentLengthLong() >= 0
                ? request.getContentLengthLong()
                : cached.length;
        String digestSource = cached.length == 0
                ? request.getMethod() + "\n" + path + "\n" + requestBytes
                : new String(cached, StandardCharsets.ISO_8859_1);
        int status = response.getStatus();
        long durationMs = Math.max(0, (System.nanoTime() - startedAt) / 1_000_000);
        return new IntegrationAuditService.AuditEvent(
                requestId,
                protocol,
                operation(request.getMethod(), path),
                caller.type(),
                caller.identifier(),
                request.getMethod(),
                path,
                request.getRemoteAddr(),
                sha256(digestSource),
                requestBytes,
                status,
                status >= 200 && status < 400,
                "HTTP_" + status,
                durationMs
        );
    }

    private Caller caller(HttpServletRequest request, String protocol, Authentication authentication) {
        if ("SOAP".equals(protocol)) {
            return new Caller("integration_client", safeIdentifier(request.getHeader("X-Integration-Client")));
        }
        if ("APP".equals(protocol)) {
            String token = request.getHeader("X-Device-Token");
            String identifier = token == null || token.isBlank()
                    ? "anonymous-device"
                    : "device-" + sha256(token).substring(0, 16);
            return new Caller("collection_device", identifier);
        }
        String actor = authentication != null && authentication.isAuthenticated()
                ? safeIdentifier(authentication.getName())
                : "anonymous-user";
        return new Caller("system_user", actor);
    }

    private String protocol(String path) {
        if (path.startsWith("/api/mobile/")) return "APP";
        if (path.startsWith("/api/soap/")) return "SOAP";
        return "FILE";
    }

    private String operation(String method, String path) {
        if (path.endsWith("/growth-records/batch")) return "APP_BATCH_UPLOAD";
        if (path.endsWith("/sync/status")) return "APP_SYNC_STATUS";
        if (path.endsWith(".wsdl")) return "SOAP_WSDL_READ";
        if (path.endsWith(".xsd")) return "SOAP_XSD_READ";
        if (path.equals("/api/soap/school")) return "SOAP_QUERY_GROWTH";
        if (path.endsWith("/upload")) return "FILE_UPLOAD";
        if (path.endsWith("/upload-json")) return "FILE_UPLOAD_JSON";
        if (path.endsWith("/download")) return "FILE_DOWNLOAD";
        if (path.endsWith("/preview")) return "FILE_PREVIEW";
        if ("DELETE".equals(method)) return "FILE_DELETE";
        if ("GET".equals(method)) return "FILE_LIST";
        return "EXTERNAL_REQUEST";
    }

    private String requestId(String supplied) {
        if (supplied != null && supplied.matches("[A-Za-z0-9._~-]{8,64}")) {
            return supplied;
        }
        return UUID.randomUUID().toString();
    }

    private String safeIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return "anonymous";
        }
        String cleaned = value.replaceAll("[^A-Za-z0-9@._~-]", "_");
        return cleaned.length() > 160 ? cleaned.substring(0, 160) : cleaned;
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.ISO_8859_1)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private record Caller(String type, String identifier) {}
}
