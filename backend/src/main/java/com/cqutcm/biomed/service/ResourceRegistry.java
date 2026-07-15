package com.cqutcm.biomed.service;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ResourceRegistry {
    private final Set<String> resources = Set.of(
            "herbs",
            "herb-encyclopedia",
            "growth-records",
            "trace-events",
            "teaching-resources",
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
            throw new IllegalArgumentException("unknown resource: " + resourceType);
        }
    }
}
