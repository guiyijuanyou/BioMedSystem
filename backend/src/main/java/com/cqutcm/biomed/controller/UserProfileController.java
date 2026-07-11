package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.mapper.SysUserMapper;
import com.cqutcm.biomed.service.AuthService;
import com.cqutcm.biomed.service.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final AuthService authService;
    private final SysUserMapper sysUserMapper;

    public UserProfileController(AuthService authService, SysUserMapper sysUserMapper) {
        this.authService = authService;
        this.sysUserMapper = sysUserMapper;
    }

    @GetMapping("/profile")
    public Map<String, Object> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String loginUsername = authService.requireLoginUsername(authorization);
        PermissionService.Actor actor = authService.requireActor(authorization);
        var user = sysUserMapper.findByUsername(loginUsername);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return buildProfileMap(user, actor);
    }

    @PutMapping("/profile")
    public Map<String, Object> updateMyProfile(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String loginUsername = authService.requireLoginUsername(authorization);
        var user = sysUserMapper.findByUsername(loginUsername);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        String phone = trimToLength(payload, "phone", 30);
        String email = trimToLength(payload, "email", 150);
        String avatarUrl = trimToLength(payload, "avatarUrl", 500);
        String title = trimToLength(payload, "title", 100);
        String researchArea = trimToLength(payload, "researchArea", 300);
        String bio = trimToLength(payload, "bio", 2000);

        sysUserMapper.updateProfile(user.getId(), phone, email, avatarUrl, title, researchArea, bio);

        PermissionService.Actor actor = authService.requireActor(authorization);
        var updated = sysUserMapper.findByUsername(loginUsername);
        return buildProfileMap(updated, actor);
    }

    @GetMapping("/{userId}/profile")
    public Map<String, Object> getUserProfile(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireActor(authorization);
        Map<String, Object> userMap = sysUserMapper.findByIdWithRoles(userId);
        if (userMap == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return buildPublicProfileMap(userMap);
    }

    private Map<String, Object> buildProfileMap(com.cqutcm.biomed.entity.SysUser user, PermissionService.Actor actor) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", user.getId());
        m.put("username", user.getUsername());
        m.put("name", user.getDisplayName());
        m.put("department", user.getDepartment());
        m.put("status", user.getStatus());
        m.put("phone", user.getPhone());
        m.put("email", user.getEmail());
        m.put("avatarUrl", user.getAvatarUrl());
        m.put("title", user.getTitle());
        m.put("researchArea", user.getResearchArea());
        m.put("bio", user.getBio());
        m.put("role", actor.role());
        m.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        m.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        return m;
    }

    private Map<String, Object> buildPublicProfileMap(Map<String, Object> userMap) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", userMap.get("id"));
        m.put("name", userMap.get("display_name"));
        m.put("department", userMap.get("department"));
        m.put("title", userMap.get("title"));
        m.put("researchArea", userMap.get("research_area"));
        m.put("bio", userMap.get("bio"));
        m.put("avatarUrl", userMap.get("avatar_url"));
        m.put("role", userMap.get("role_code"));
        m.put("roleLabel", userMap.get("role_name"));
        return m;
    }

    private String truncate(String value, int maxLen) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed.length() > maxLen ? trimmed.substring(0, maxLen) : trimmed;
    }

    private String trimToLength(Map<String, Object> m, String key, int maxLen) {
        return truncate(str(m, key), maxLen);
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : String.valueOf(v);
    }
}
