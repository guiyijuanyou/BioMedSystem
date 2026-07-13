package com.cqutcm.biomed.config;

import java.nio.file.Path;

import org.springframework.stereotype.Component;

/**
 * 统一解析应用数据目录
 */
@Component
public class AppDataPathResolver {

    private final Path projectRoot;

    public AppDataPathResolver() {
        Path userDir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (userDir.endsWith("backend")) {
            userDir = userDir.getParent();
        }
        this.projectRoot = userDir;
    }

    public Path resolve(String relativePath) {
        Path path = Path.of(relativePath);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return projectRoot.resolve(relativePath).normalize();
    }
}
