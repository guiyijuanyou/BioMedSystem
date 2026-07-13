package com.cqutcm.biomed.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IntegrationClientServiceTest {
    private static final String CLIENT_CODE = "CQUTCM-SCHOOL";
    private static final String SECRET = "test-integration-secret";
    private static final String BODY = "<queryGrowthData><keyword>黄连</keyword></queryGrowthData>";
    private static final String PATH = "/api/soap/school";

    @Mock
    private JdbcTemplate jdbc;

    private IntegrationClientService service;

    @BeforeEach
    void setUp() {
        service = new IntegrationClientService(jdbc, new PermissionService(), "unit-test-master-key");
    }

    @Test
    void canonicalRequestIncludesBodyDigest() {
        String canonical = IntegrationClientService.canonicalRequest(
                "POST", PATH, "100", "nonce-1234567890", BODY);

        assertTrue(canonical.startsWith("POST\n/api/soap/school\n100\nnonce-1234567890\n"));
        assertEquals(64, canonical.substring(canonical.lastIndexOf('\n') + 1).length());
    }

    @Test
    void expiredRequestIsRejectedBeforeDatabaseLookup() {
        String expired = String.valueOf(Instant.now().minusSeconds(600).getEpochSecond());

        assertThrows(AuthenticationRequiredException.class, () -> service.authenticate(
                CLIENT_CODE, expired, "nonce-1234567890", "00", "POST", PATH, BODY, "127.0.0.1"));
    }

    @Test
    void validSignatureAuthenticatesAndStoresNonce() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = "nonce-1234567890";
        prepareActiveClient();
        String canonical = IntegrationClientService.canonicalRequest("POST", PATH, timestamp, nonce, BODY);
        String signature = IntegrationClientService.signature(SECRET, canonical);

        IntegrationClientService.IntegrationPrincipal principal = service.authenticate(
                CLIENT_CODE, timestamp, nonce, signature, "POST", PATH, BODY, "127.0.0.1");

        assertEquals("client-1", principal.id());
        assertEquals(CLIENT_CODE, principal.clientCode());
    }

    @Test
    void invalidSignatureIsRejected() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        prepareActiveClient();

        assertThrows(AuthenticationRequiredException.class, () -> service.authenticate(
                CLIENT_CODE, timestamp, "nonce-1234567890", "00", "POST", PATH, BODY, "127.0.0.1"));
    }

    @Test
    void reusedNonceIsRejected() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = "nonce-1234567890";
        prepareActiveClient();
        doThrow(new DuplicateKeyException("duplicate nonce")).when(jdbc).update(
                contains("INSERT INTO integration_nonce"),
                anyString(), anyString(), anyString(), eq(Long.parseLong(timestamp)),
                org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class));
        String canonical = IntegrationClientService.canonicalRequest("POST", PATH, timestamp, nonce, BODY);
        String signature = IntegrationClientService.signature(SECRET, canonical);

        AuthenticationRequiredException error = assertThrows(AuthenticationRequiredException.class,
                () -> service.authenticate(CLIENT_CODE, timestamp, nonce, signature,
                        "POST", PATH, BODY, "127.0.0.1"));

        assertEquals("replayed integration request", error.getMessage());
    }

    @Test
    void disabledClientIsRejected() {
        when(jdbc.queryForList(anyString(), eq(CLIENT_CODE))).thenReturn(List.of(Map.of(
                "id", "client-1",
                "status", "disabled"
        )));
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        assertThrows(AuthorizationDeniedException.class, () -> service.authenticate(
                CLIENT_CODE, timestamp, "nonce-1234567890", "00", "POST", PATH, BODY, "127.0.0.1"));
    }

    private void prepareActiveClient() {
        IntegrationClientService.EncryptedSecret encrypted = service.encryptSecret(SECRET);
        when(jdbc.queryForList(anyString(), eq(CLIENT_CODE))).thenReturn(List.of(Map.of(
                "id", "client-1",
                "client_code", CLIENT_CODE,
                "client_name", "校内数据中心",
                "secret_ciphertext", encrypted.ciphertext(),
                "secret_iv", encrypted.iv(),
                "status", "active",
                "allowed_ips", "127.0.0.1"
        )));
    }
}
