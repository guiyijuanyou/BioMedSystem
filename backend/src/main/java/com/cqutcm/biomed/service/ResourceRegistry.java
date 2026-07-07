package com.cqutcm.biomed.service;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ResourceRegistry {
    private final Set<String> resources = Set.of(
            "herbs",
            "growth-records",
            "spectrum-comparisons",
            "growth-analysis",
            "courses",
            "projects",
            "trainings",
            "evaluations",
            "achievements",
            "users",
            "standards"
    );

    public void requireSupported(String resourceType) {
        if (!resources.contains(resourceType)) {
            throw new IllegalArgumentException("未知资源: " + resourceType);
        }
    }
}
