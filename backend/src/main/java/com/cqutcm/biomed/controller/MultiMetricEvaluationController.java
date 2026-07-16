package com.cqutcm.biomed.controller;
import com.cqutcm.biomed.service.*;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/multi-evaluations") public class MultiMetricEvaluationController{
 private final AuthService auth;private final MultiMetricEvaluationService service;public MultiMetricEvaluationController(AuthService a,MultiMetricEvaluationService s){auth=a;service=s;}
 @GetMapping public Map<String,Object> overview(@RequestHeader(value="Authorization",required=false)String t){return service.overview(auth.requireActor(t));}
 @GetMapping("/{id}") public Map<String,Object> detail(@PathVariable String id,@RequestHeader(value="Authorization",required=false)String t){return service.detail(id,auth.requireActor(t));}
 @PostMapping("/evaluate") public Map<String,Object> evaluate(@RequestBody Map<String,Object>b,@RequestHeader(value="Authorization",required=false)String t){return service.evaluate(String.valueOf(b.get("schemeId")),String.valueOf(b.get("batchId")),auth.requireActor(t));}
 @DeleteMapping("/{id}") public Map<String,Object> delete(@PathVariable String id,@RequestHeader(value="Authorization",required=false)String t){service.delete(id,auth.requireActor(t));return Map.of("message","evaluation deleted");}
}