package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.AuthService;
import com.cqutcm.biomed.service.QualityMetricService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/quality-metrics")
public class QualityMetricController {
    private final AuthService auth; private final QualityMetricService service;
    public QualityMetricController(AuthService auth, QualityMetricService service){this.auth=auth;this.service=service;}
    @GetMapping public Map<String,Object> overview(@RequestHeader(value="Authorization",required=false)String token){return service.overview(auth.requireActor(token));}
    @PostMapping public Map<String,Object> create(@RequestBody Map<String,Object> body,@RequestHeader(value="Authorization",required=false)String token){return service.createDefinition(body,auth.requireActor(token));}
    @PostMapping("/calculate/{batchId}") public Map<String,Object> calculate(@PathVariable String batchId,@RequestHeader(value="Authorization",required=false)String token){return Map.of("items",service.calculate(batchId,auth.requireActor(token)));}
}
