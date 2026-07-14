package com.cqutcm.biomed.config;

import com.cqutcm.biomed.entity.SysUser;
import com.cqutcm.biomed.mapper.SysUserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    private static final Map<String, String> DEFAULT_PASSWORDS = Map.of(
        "admin@cqutcm", "Admin@123456",
        "teacher@cqutcm", "Teacher@123456",
        "researcher@cqutcm", "Res@123456",
        "student@cqutcm", "Student@123"
    );

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
