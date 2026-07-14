package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.SysUser;
import com.cqutcm.biomed.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // redis 传 null → AuthService 自动降级到本地内存
        authService = new AuthService(userMapper, passwordEncoder, null, objectMapper, 480L, 8);
    }

    @Test
    void rejectsForgedUsernameToken() {
        assertThrows(AuthenticationRequiredException.class,
                () -> authService.requireActor("Bearer admin:anything"));
    }

    @Test
    void acceptsLoginTokenAndRevokesItOnLogout() {
        SysUser user = enabledUser();
        when(userMapper.findByUsername("admin@cqutcm")).thenReturn(user);
        when(userMapper.findRolesByUserId("user-admin"))
                .thenReturn(List.of(Map.of("code", "admin", "name", "管理员")));
        when(passwordEncoder.matches("secret", "stored-hash")).thenReturn(true);

        Map<String, Object> login = authService.login(Map.of(
                "username", "admin@cqutcm",
                "password", "secret"
        ));
        String token = String.valueOf(login.get("token"));

        assertFalse(token.contains(":"));
        assertEquals("admin", authService.requireActor("Bearer " + token).role());

        authService.logout("Bearer " + token);
        assertThrows(AuthenticationRequiredException.class,
                () -> authService.requireActor("Bearer " + token));
    }

    @Test
    void rejectsIncorrectPassword() {
        SysUser user = enabledUser();
        when(userMapper.findByUsername("admin@cqutcm")).thenReturn(user);
        when(userMapper.findRolesByUserId("user-admin"))
                .thenReturn(List.of(Map.of("code", "admin", "name", "管理员")));
        when(passwordEncoder.matches("wrong", "stored-hash")).thenReturn(false);

        assertThrows(AuthenticationRequiredException.class, () -> authService.login(Map.of(
                "username", "admin@cqutcm",
                "password", "wrong"
        )));
    }

    private SysUser enabledUser() {
        SysUser user = new SysUser();
        user.setId("user-admin");
        user.setUsername("admin@cqutcm");
        user.setDisplayName("系统管理员");
        user.setPasswordHash("stored-hash");
        user.setStatus("enabled");
        return user;
    }
}
