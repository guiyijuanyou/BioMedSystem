package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.GenericRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResourceController {
    private final GenericRecordService service;

    public ResourceController(GenericRecordService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return service.summary();
    }

    @PostMapping("/backup")
    public Map<String, String> backup() {
        Path backup = service.backup();
        return Map.of("message", "备份完成", "file", backup.toString());
    }

    @GetMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}")
    public Map<String, Object> list(@PathVariable String resourceType) {
        return Map.of("items", service.list(resourceType));
    }

    @PostMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}")
    public Map<String, Object> create(@PathVariable String resourceType, @RequestBody Map<String, Object> payload) {
        return service.create(resourceType, payload);
    }

    @PutMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}")
    public Map<String, Object> update(@PathVariable String resourceType, @RequestBody Map<String, Object> payload) {
        return service.update(resourceType, payload);
    }
}

