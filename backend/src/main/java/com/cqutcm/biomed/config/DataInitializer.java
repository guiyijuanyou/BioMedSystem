package com.cqutcm.biomed.config;

import com.cqutcm.biomed.entity.SysUser;
import com.cqutcm.biomed.mapper.SysUserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    private static final Map<String, String> DEFAULT_PASSWORDS = new java.util.LinkedHashMap<>();
    static {
        DEFAULT_PASSWORDS.put("admin@cqutcm", "Admin@123456");
        DEFAULT_PASSWORDS.put("teacher@cqutcm", "Teacher@123456");
        DEFAULT_PASSWORDS.put("researcher@cqutcm", "Res@123456");
        DEFAULT_PASSWORDS.put("student@cqutcm", "Student@123");
        // test-data-extra 补充用户
        DEFAULT_PASSWORDS.put("zhang@cqutcm", "Teacher@123456");
        DEFAULT_PASSWORDS.put("liu@cqutcm", "Teacher@123456");
        DEFAULT_PASSWORDS.put("chen@cqutcm", "Res@123456");
        DEFAULT_PASSWORDS.put("stu-a@cqutcm", "Student@123");
        DEFAULT_PASSWORDS.put("stu-b@cqutcm", "Student@123");
        DEFAULT_PASSWORDS.put("stu-c@cqutcm", "Student@123");
        DEFAULT_PASSWORDS.put("stu-d@cqutcm", "Student@123");
        DEFAULT_PASSWORDS.put("stu-e@cqutcm", "Student@123");
    }

    @Override
    public void run(String... args) {
        for (String username : DEFAULT_PASSWORDS.keySet()) {
            ensurePasswordHash(username, DEFAULT_PASSWORDS.get(username));
        }
    }

    private void ensurePasswordHash(String username, String defaultPassword) {
        SysUser user = userMapper.findByUsername(username);
        if (user == null) return;
        String hash = user.getPasswordHash();
        if (hash == null || hash.isBlank() || !hash.startsWith("$2a$")) {
            userMapper.updatePasswordHash(user.getId(), passwordEncoder.encode(defaultPassword));
        }
    }
}
