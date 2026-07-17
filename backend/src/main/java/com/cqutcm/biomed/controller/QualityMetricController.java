package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.AuthService;
import com.cqutcm.biomed.service.QualityMetricService;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.*;

@RestController
@RequestMapping("/api/quality-metrics")
public class QualityMetricController {
    private final AuthService auth; private final QualityMetricService service;
    public QualityMetricController(AuthService auth, QualityMetricService service){this.auth=auth;this.service=service;}

    @GetMapping public Map<String,Object> overview(@RequestHeader(value="Authorization",required=false)String token){return service.overview(auth.requireActor(token));}

    @PostMapping("/calculate/{batchId}") public Map<String,Object> calculate(@PathVariable String batchId,@RequestHeader(value="Authorization",required=false)String token){return Map.of("items",service.calculate(batchId,auth.requireActor(token)));}

    /** 保存/更新某药材的全部指标配置 */
    @PutMapping("/herb-config")
    public Map<String,Object> saveHerbConfig(@RequestBody Map<String,Object> body,@RequestHeader(value="Authorization",required=false)String token){
        return service.saveHerbConfig(String.valueOf(body.get("herbId")), body, auth.requireActor(token));
    }

    /** 获取某药材的活跃指标配置 */
    @GetMapping("/herb-metrics/{herbId}")
    public Map<String,Object> getHerbMetrics(@PathVariable String herbId,@RequestHeader(value="Authorization",required=false)String token){
        auth.requireActor(token);
        List<Map<String,Object>> list = service.getHerbMetrics(herbId);
        System.out.println("[数据分析-API] herbId=" + herbId + " 查到" + list.size() + "条指标:");
        for (Map<String,Object> m : list) {
          System.out.println("  " + m.get("metricCode") + " " + m.get("metricName") + " 区间[" + m.get("minimumValue") + ", " + m.get("maximumValue") + "] 权重=" + m.get("weightValue"));
        }
        return Map.of("metrics", list);
    }
}
