package com.cqutcm.biomed.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IntegrationClientService {
    private static final long REQUEST_WINDOW_SECONDS = 300;
    private static final int GCM_TAG_BITS = 128;

    private final JdbcTemplate jdbc;
    private final PermissionService permissions;
    private final SecureRandom random = new SecureRandom();
    private final SecretKeySpec encryptionKey;

    public IntegrationClientService(
            JdbcTemplate jdbc,
            PermissionService permissions,
            @Value("${app.integration.master-key:change-this-integration-master-key}") String masterKey
    ) {
        this.jdbc = jdbc;
        this.permissions = permissions;
        this.encryptionKey = new SecretKeySpec(sha256(masterKey), "AES");
    }

    public List<Map<String, Object>> list(PermissionService.Actor actor) {
        requireAdmin(actor);
        return jdbc.queryForList("""
                SELECT id, client_code AS clientCode, client_name AS clientName, status,
                       allowed_ips AS allowedIps, last_used_at AS lastUsedAt,
                       secret_rotated_at AS secretRotatedAt, created_by AS createdBy,
                       created_at AS createdAt
                  FROM integration_client
                 ORDER BY created_at DESC
                """);
    }

    @Transactional
    public Map<String, Object> register(Map<String, Object> body, PermissionService.Actor actor) {
        requireAdmin(actor);
        String clientCode = required(body, "clientCode");
        String clientName = required(body, "clientName");
        String secret = newSecret();
        EncryptedSecret encrypted = encryptSecret(secret);
        String id = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO integration_client(
                    id, client_code, client_name, secret_ciphertext, secret_iv,
                    allowed_ips, created_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, clientCode, clientName, encrypted.ciphertext(), encrypted.iv(),
                normalizedIps(body.get("allowedIps")), actor.name());
        return Map.of(
                "id", id,
                "clientCode", clientCode,
                "clientName", clientName,
                "secret", secret,
                "warning", "secret is shown only once"
        );
    }

    @Transactional
    public Map<String, Object> rotateSecret(String id, PermissionService.Actor actor) {
        requireAdmin(actor);
        String secret = newSecret();
        EncryptedSecret encrypted = encryptSecret(secret);
        int updated = jdbc.update("""
                UPDATE integration_client
                   SET secret_ciphertext = ?, secret_iv = ?, secret_rotated_at = NOW(), updated_at = NOW()
                 WHERE id = ?
                """, encrypted.ciphertext(), encrypted.iv(), id);
        if (updated == 0) {
            throw new IllegalArgumentException("integration client not found");
        }
        return Map.of("secret", secret, "warning", "secret is shown only once");
    }

    @Transactional
    public void setStatus(String id, String status, PermissionService.Actor actor) {
        requireAdmin(actor);
        if (!List.of("active", "disabled").contains(status)) {
            throw new IllegalArgumentException("invalid integration client status");
        }
        if (jdbc.update("UPDATE integration_client SET status = ?, updated_at = NOW() WHERE id = ?",
                status, id) == 0) {
            throw new IllegalArgumentException("integration client not found");
        }
    }

    @Transactional
    public IntegrationPrincipal authenticate(
            String clientCode,
            String timestampHeader,
            String nonce,
            String signature,
            String method,
            String path,
            String body,
            String remoteAddress
    ) {
        validateHeaders(clientCode, timestampHeader, nonce, signature);
        long timestamp = parseTimestamp(timestampHeader);
        validateTimestamp(timestamp);
        Map<String, Object> client = requireActiveClient(clientCode);
        validateRemoteAddress(client.get("allowed_ips"), remoteAddress);

        String secret = decryptSecret(
                String.valueOf(client.get("secret_ciphertext")),
                String.valueOf(client.get("secret_iv")));
        String canonical = canonicalRequest(method, path, timestampHeader, nonce, body);
        if (!MessageDigest.isEqual(hmac(secret, canonical), parseSignature(signature))) {
            throw new AuthenticationRequiredException("invalid integration signature");
        }

        removeExpiredNonces();
        rememberNonce(String.valueOf(client.get("id")), nonce, timestamp);
        jdbc.update("UPDATE integration_client SET last_used_at = NOW() WHERE id = ?", client.get("id"));
        return new IntegrationPrincipal(String.valueOf(client.get("id")), clientCode,
                String.valueOf(client.get("client_name")));
    }

    static String canonicalRequest(String method, String path, String timestamp, String nonce, String body) {
        return method.toUpperCase() + "\n"
                + path + "\n"
                + timestamp + "\n"
                + nonce + "\n"
                + HexFormat.of().formatHex(sha256(body == null ? "" : body));
    }

    static String signature(String secret, String canonicalRequest) {
        return HexFormat.of().formatHex(hmac(secret, canonicalRequest));
    }

    EncryptedSecret encryptSecret(String secret) {
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
            return new EncryptedSecret(
                    Base64.getEncoder().encodeToString(encrypted),
                    Base64.getEncoder().encodeToString(iv));
        } catch (Exception ex) {
            throw new IllegalStateException("failed to encrypt integration secret", ex);
        }
    }

    private String decryptSecret(String ciphertext, String iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey,
                    new GCMParameterSpec(GCM_TAG_BITS, Base64.getDecoder().decode(iv)));
            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("failed to decrypt integration secret", ex);
        }
    }

    private Map<String, Object> requireActiveClient(String clientCode) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM integration_client WHERE client_code = ?", clientCode);
        if (rows.isEmpty()) {
            throw new AuthenticationRequiredException("unknown integration client");
        }
        Map<String, Object> client = rows.getFirst();
        if (!"active".equals(client.get("status"))) {
            throw new AuthorizationDeniedException("integration client is disabled");
        }
        return client;
    }

    private void rememberNonce(String clientId, String nonce, long timestamp) {
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp + REQUEST_WINDOW_SECONDS), ZoneId.systemDefault());
        try {
            jdbc.update("""
                    INSERT INTO integration_nonce(
                        id, client_id, nonce_value, request_timestamp, expires_at
                    ) VALUES (?, ?, ?, ?, ?)
                    """, UUID.randomUUID().toString(), clientId, nonce, timestamp, expiresAt);
        } catch (DuplicateKeyException ex) {
            throw new AuthenticationRequiredException("replayed integration request");
        }
    }

    private void removeExpiredNonces() {
        jdbc.update("DELETE FROM integration_nonce WHERE expires_at < NOW()");
    }

    private void validateHeaders(String clientCode, String timestamp, String nonce, String signature) {
        if (isBlank(clientCode) || isBlank(timestamp) || isBlank(nonce) || isBlank(signature)) {
            throw new AuthenticationRequiredException("integration signature headers are required");
        }
        if (nonce.length() < 16 || nonce.length() > 128 || !nonce.matches("[A-Za-z0-9._~-]+")) {
            throw new AuthenticationRequiredException("invalid integration nonce");
        }
    }

    private long parseTimestamp(String timestamp) {
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException ex) {
            throw new AuthenticationRequiredException("invalid integration timestamp");
        }
    }

    private void validateTimestamp(long timestamp) {
        if (Math.abs(Instant.now().getEpochSecond() - timestamp) > REQUEST_WINDOW_SECONDS) {
            throw new AuthenticationRequiredException("integration request has expired");
        }
    }

    private void validateRemoteAddress(Object allowedIpsValue, String remoteAddress) {
        String allowedIps = allowedIpsValue == null ? "" : String.valueOf(allowedIpsValue).trim();
        if (allowedIps.isBlank()) {
            return;
        }
        boolean allowed = List.of(allowedIps.split(",")).stream()
                .map(String::trim)
                .anyMatch(remoteAddress::equals);
        if (!allowed) {
            throw new AuthorizationDeniedException("integration client IP is not allowed");
        }
    }

    private byte[] parseSignature(String signature) {
        try {
            return HexFormat.of().parseHex(signature.trim().toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new AuthenticationRequiredException("invalid integration signature format");
        }
    }

    private String normalizedIps(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection<?> collection) {
            String joined = collection.stream().map(String::valueOf).map(String::trim)
                    .filter(item -> !item.isBlank()).reduce((left, right) -> left + "," + right).orElse("");
            return joined.isBlank() ? null : joined;
        }
        String text = String.valueOf(value).trim();
        return text.isBlank() ? null : text;
    }

    private String required(Map<String, Object> body, String key) {
        String value = String.valueOf(body.getOrDefault(key, "")).trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }

    private void requireAdmin(PermissionService.Actor actor) {
        if (!permissions.isAdmin(actor)) {
            throw new AuthorizationDeniedException("only admin can manage integration clients");
        }
    }

    private String newSecret() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static byte[] hmac(String secret, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    record EncryptedSecret(String ciphertext, String iv) {}

    public record IntegrationPrincipal(String id, String clientCode, String clientName) {}
}
