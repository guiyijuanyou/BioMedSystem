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

    @Override
    public void run(String... args) {
        ensurePasswordHash("admin");
        ensurePasswordHash("teacher");
        ensurePasswordHash("researcher");
        ensurePasswordHash("student");
    }

    private void ensurePasswordHash(String username) {
        SysUser user = userMapper.findByUsername(username);
        if (user == null) return;
        String hash = user.getPasswordHash();
        if (hash == null || hash.isBlank() || !hash.startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode("123456"));
            userMapper.update(user);
        }
    }
}
